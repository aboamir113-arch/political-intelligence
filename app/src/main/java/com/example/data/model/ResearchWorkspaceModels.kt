package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * In-memory active state of the Research Workspace
 */
data class ResearchWorkspaceState(
    val selectedArticles: List<Article> = emptyList(),
    val selectedEvents: List<PoliticalEvent> = emptyList(),
    val selectedPoliticalFile: PoliticalFile? = null,
    val selectedPersons: List<Person> = emptyList(),
    val selectedOrganizations: List<Organization> = emptyList(),
    val selectedCountries: List<Country> = emptyList(),
    val selectedTopics: List<Topic> = emptyList(),
    val selectedSources: List<Source> = emptyList(),
    val dateFilter: DateFilter = DateFilter.ALL_TIME,
    val customFromUtc: Long? = null,
    val customToUtc: Long? = null,
    val verificationFilter: VerificationStatus? = null,
    val freeformKeywords: String = ""
) {
    val totalItemsCount: Int
        get() = selectedArticles.size +
                selectedEvents.size +
                (if (selectedPoliticalFile != null) 1 else 0) +
                selectedPersons.size +
                selectedOrganizations.size +
                selectedCountries.size +
                selectedTopics.size +
                selectedSources.size

    val isEmpty: Boolean
        get() = totalItemsCount == 0 && freeformKeywords.isBlank()

    fun clear(): ResearchWorkspaceState = ResearchWorkspaceState()
}

/**
 * Persisted Saved Research Session (Entity)
 */
@Entity(
    tableName = "saved_research",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class SavedResearch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val title: String,
    val mainQuestion: String,
    val articleIdsCsv: String = "",
    val eventIdsCsv: String = "",
    val politicalFileId: Long? = null,
    val personIdsCsv: String = "",
    val sourceIdsCsv: String = "",
    val topicIdsCsv: String = "",
    val organizationIdsCsv: String = "",
    val countryCodesCsv: String = "",
    val dateFilterName: String = DateFilter.ALL_TIME.name,
    val filtersJson: String = "{}",
    val resultingSummary: String? = null,
    val resultingAnalysisJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
