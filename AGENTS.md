# AGENTS.md

Guidance for AI agents working on this repository.

# Project

- PROJECT_NAME: Kalkulasa
- REPO: nekonaru/kalkulasa
- AUTHOR_NAME: Nicolas Dwi Dharma
- AUTHOR_GITHUB: nekonaru
- PROJECT_KIND: app
- STACK: Kotlin (Jetpack Compose, Material 3)
- PLATFORMS: Android
- MIN_OS: Android 7.0 (API 24)
- DEFAULT_BRANCH: main
- LICENSE: MIT
- UI_LANGUAGES: Bahasa Indonesia (default), English
- VERIFICATION: local-and-ci
- PIPELINE: direct

Assumptions: the owner asked for direct, gradual pushes to the final
repository, so `PIPELINE` is `direct`. Owner's language directive (overrides
the usual defaults): Bahasa Indonesia is the default everywhere, including the
app UI, README, repository metadata and release notes; English stays as the
bilingual counterpart (`README.en.md`, `CHANGELOG.en.md`, release notes list
Bahasa Indonesia first). Documentation files are synced between the two.
