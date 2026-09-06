package com.example.data.intelligence

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

/**
 * Political Graph Engine
 * Builds node & edge representations for persons, organizations, countries, files, events, and statements.
 * Manages interactive layout coordinates, relationship filtering, and AI discovery queue.
 */
class PoliticalGraphEngine(
    private val relationshipDao: PoliticalRelationshipDao,
    private val personDao: PersonDao,
    private val organizationDao: OrganizationDao,
    private val countryDao: CountryDao,
    private val politicalFileDao: PoliticalFileDao,
    private val politicalEventDao: PoliticalEventDao,
    private val articleDao: ArticleDao,
    private val statementDao: StatementDao
) {

    /**
     * Compute a layout graph of nodes and edges matching the specified filters.
     */
    suspend fun buildGraph(
        selectedEntityType: EntityType? = null,
        selectedRelationshipType: RelationshipType? = null,
        minConfidence: Float = 0.5f,
        timeframeStartUtc: Long = 0L,
        onlyDirectEvidence: Boolean = false,
        searchQuery: String = ""
    ): Pair<List<GraphNode>, List<GraphEdge>> = withContext(Dispatchers.Default) {
        val allRelationships = relationshipDao.getAllRelationshipsList()

        val filteredRels = allRelationships.filter { rel ->
            val matchesEntity = selectedEntityType == null ||
                    rel.sourceEntityType == selectedEntityType ||
                    rel.targetEntityType == selectedEntityType
            val matchesRelType = selectedRelationshipType == null || rel.relationshipType == selectedRelationshipType
            val matchesConfidence = rel.confidence >= minConfidence
            val matchesTimeframe = timeframeStartUtc <= 0L || rel.validFromUtc >= timeframeStartUtc
            val matchesEvidence = !onlyDirectEvidence || rel.evidenceNature == EvidenceNature.DIRECT_EVIDENCE
            val matchesQuery = searchQuery.isBlank() ||
                    rel.sourceEntityName.contains(searchQuery, ignoreCase = true) ||
                    rel.targetEntityName.contains(searchQuery, ignoreCase = true) ||
                    rel.relationshipType.nameAr.contains(searchQuery, ignoreCase = true)

            matchesEntity && matchesRelType && matchesConfidence && matchesTimeframe && matchesEvidence && matchesQuery &&
                    rel.discoveryStatus != DiscoveryStatus.REJECTED
        }

        // Collect unique nodes
        val nodeMap = mutableMapOf<String, GraphNode>()

        filteredRels.forEach { rel ->
            val sourceKey = "${rel.sourceEntityType.name}_${rel.sourceEntityId}"
            if (!nodeMap.containsKey(sourceKey)) {
                nodeMap[sourceKey] = GraphNode(
                    id = sourceKey,
                    entityType = rel.sourceEntityType,
                    entityId = rel.sourceEntityId,
                    label = rel.sourceEntityName,
                    roleOrCategory = rel.sourceEntityType.displayNameAr(),
                    importanceScore = 1.0f
                )
            }

            val targetKey = "${rel.targetEntityType.name}_${rel.targetEntityId}"
            if (!nodeMap.containsKey(targetKey)) {
                nodeMap[targetKey] = GraphNode(
                    id = targetKey,
                    entityType = rel.targetEntityType,
                    entityId = rel.targetEntityId,
                    label = rel.targetEntityName,
                    roleOrCategory = rel.targetEntityType.displayNameAr(),
                    importanceScore = 0.9f
                )
            }
        }

        // If no relationships matched query but we have seed entities, ensure search targets appear
        if (nodeMap.isEmpty() && searchQuery.isNotBlank()) {
            val persons = personDao.getAllPersonsList()
            persons.filter { it.nameAr.contains(searchQuery, ignoreCase = true) }.forEach { p ->
                val key = "PERSON_${p.id}"
                nodeMap[key] = GraphNode(
                    id = key,
                    entityType = EntityType.PERSON,
                    entityId = p.id,
                    label = p.nameAr,
                    roleOrCategory = p.currentRoleAr
                )
            }
        }

        // Layout nodes in a radial circular arrangement with clustered jitter for clean visualization
        val nodeList = nodeMap.values.toList()
        val totalNodes = nodeList.size
        val radius = when {
            totalNodes <= 6 -> 220f
            totalNodes <= 14 -> 340f
            else -> 480f
        }

        val positionedNodes = nodeList.mapIndexed { index, node ->
            val angle = (2 * Math.PI * index / totalNodes.coerceAtLeast(1)).toFloat()
            val r = radius * (0.85f + (index % 3) * 0.15f)
            node.copy(
                x = r * cos(angle),
                y = r * sin(angle)
            )
        }

        val edges = filteredRels.mapNotNull { rel ->
            val sKey = "${rel.sourceEntityType.name}_${rel.sourceEntityId}"
            val tKey = "${rel.targetEntityType.name}_${rel.targetEntityId}"
            if (nodeMap.containsKey(sKey) && nodeMap.containsKey(tKey)) {
                GraphEdge(
                    id = rel.id,
                    sourceNodeId = sKey,
                    targetNodeId = tKey,
                    relationshipType = rel.relationshipType,
                    label = rel.relationshipType.displayNameAr(),
                    isDirect = rel.evidenceNature == EvidenceNature.DIRECT_EVIDENCE,
                    confidence = rel.confidence,
                    relationship = rel
                )
            } else null
        }

        Pair(positionedNodes, edges)
    }

    /**
     * AI-Assisted Discovery: Scans recent articles, statements and events to detect
     * co-occurring actors and proposes new candidate relationships for Analyst Review.
     */
    suspend fun discoverRelationshipsFromCorpus(): List<PoliticalRelationship> = withContext(Dispatchers.IO) {
        val articles = articleDao.getAllArticles(limit = 40)
        val persons = personDao.getAllPersonsList()
        val files = politicalFileDao.getAllFilesList()
        val existingRels = relationshipDao.getAllRelationshipsList()

        val proposed = mutableListOf<PoliticalRelationship>()

        for (art in articles) {
            val text = "${art.title} ${art.snippet}"
            val mentionedPersons = persons.filter { p ->
                text.contains(p.nameAr, ignoreCase = true) || text.contains(p.nameEn, ignoreCase = true)
            }

            if (mentionedPersons.size >= 2) {
                for (i in 0 until mentionedPersons.size - 1) {
                    val p1 = mentionedPersons[i]
                    val p2 = mentionedPersons[i + 1]

                    // Check if already documented
                    val exists = existingRels.any {
                        (it.sourceEntityId == p1.id && it.targetEntityId == p2.id) ||
                                (it.sourceEntityId == p2.id && it.targetEntityId == p1.id)
                    }

                    if (!exists && proposed.none { it.sourceEntityId == p1.id && it.targetEntityId == p2.id }) {
                        // Infer candidate relationship type from keywords
                        val relType = when {
                            text.contains("لقاء") || text.contains("اجتمع") || text.contains("محادثات") -> RelationshipType.MEETS_WITH
                            text.contains("تفاوض") || text.contains("مباحثات") -> RelationshipType.NEGOTIATES_WITH
                            text.contains("حليف") || text.contains("شراكة") || text.contains("تعاون") -> RelationshipType.ALLIED_WITH
                            text.contains("انتقد") || text.contains("هاجم") || text.contains("رفض") -> RelationshipType.CRITICIZES
                            text.contains("يعارض") || text.contains("خلاف") -> RelationshipType.OPPOSES
                            text.contains("اتفق") || text.contains("توافق") -> RelationshipType.AGREES_WITH
                            else -> RelationshipType.MENTIONED_WITH
                        }

                        val candidate = PoliticalRelationship(
                            sourceEntityType = EntityType.PERSON,
                            sourceEntityId = p1.id,
                            sourceEntityName = p1.nameAr,
                            targetEntityType = EntityType.PERSON,
                            targetEntityId = p2.id,
                            targetEntityName = p2.nameAr,
                            relationshipType = relType,
                            confidence = if (relType == RelationshipType.MEETS_WITH || relType == RelationshipType.NEGOTIATES_WITH) 0.85f else 0.70f,
                            evidenceNature = EvidenceNature.AI_INFERENCE,
                            sourceArticleId = art.id,
                            sourceName = art.sourceName,
                            evidenceSnippet = "تم رصد ذكرهما المشترك في التغطية: \"${art.title.take(100)}\"",
                            discoveryMethod = "AI_CO_OCCURRENCE_EXTRACTION",
                            discoveryStatus = DiscoveryStatus.DISCOVERED,
                            discoveredAt = System.currentTimeMillis(),
                            confirmedAt = null
                        )
                        proposed.add(candidate)
                    }
                }
            }

            // Check person + political file association
            if (art.politicalFileId != null) {
                val file = files.find { it.id == art.politicalFileId }
                if (file != null) {
                    mentionedPersons.forEach { p ->
                        val exists = existingRels.any {
                            it.sourceEntityId == p.id && it.targetEntityId == file.id
                        }
                        if (!exists && proposed.none { it.sourceEntityId == p.id && it.targetEntityId == file.id }) {
                            proposed.add(
                                PoliticalRelationship(
                                    sourceEntityType = EntityType.PERSON,
                                    sourceEntityId = p.id,
                                    sourceEntityName = p.nameAr,
                                    targetEntityType = EntityType.POLITICAL_FILE,
                                    targetEntityId = file.id,
                                    targetEntityName = file.titleAr,
                                    relationshipType = RelationshipType.INVOLVED_IN,
                                    confidence = 0.80f,
                                    evidenceNature = EvidenceNature.AI_INFERENCE,
                                    sourceArticleId = art.id,
                                    sourceFileId = file.id,
                                    sourceName = art.sourceName,
                                    evidenceSnippet = "انخراط ملحوظ في سياق ملف ${file.titleAr} ضمن برقية: ${art.title.take(90)}",
                                    discoveryMethod = "FILE_ATTRIBUTION_MINING",
                                    discoveryStatus = DiscoveryStatus.DISCOVERED
                                )
                            )
                        }
                    }
                }
            }
        }

        if (proposed.isNotEmpty()) {
            relationshipDao.insertRelationships(proposed)
        }
        proposed
    }

    /**
     * Human-In-The-Loop Approval Workflow
     */
    suspend fun confirmDiscovery(id: Long, analystNotes: String? = null) {
        val rel = relationshipDao.getRelationshipById(id) ?: return
        relationshipDao.updateRelationship(
            rel.copy(
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                confirmedAt = System.currentTimeMillis(),
                analystNotes = analystNotes ?: rel.analystNotes
            )
        )
    }

    suspend fun rejectDiscovery(id: Long, rejectionReason: String? = null) {
        val rel = relationshipDao.getRelationshipById(id) ?: return
        relationshipDao.updateRelationship(
            rel.copy(
                discoveryStatus = DiscoveryStatus.REJECTED,
                analystNotes = rejectionReason ?: "مرفوضة من المحلل"
            )
        )
    }

    suspend fun markUnderReview(id: Long) {
        relationshipDao.updateStatus(id, DiscoveryStatus.UNDER_REVIEW)
    }
}
