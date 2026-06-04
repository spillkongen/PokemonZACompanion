# Pokémon Legends Z-A Companion

Unofficial companion app for **Pokémon Legends: Z-A** — MapGenie map, Pokédex, fashion, missions, and guides.

## Features

- **Map** — Interactive Lumiose City map (MapGenie)
- **Pokédex** — Live data from Bulbapedia
- **Fashion** — In-game outfits from Serebii
- **Missions** — Main and side missions
- **Guides** — Wiki and MapGenie links
- **In-app updates** — Prompts when a newer APK is on GitHub

## GitHub setup (one time)

### 1. Configure your repo name

Edit `gradle.properties`:

```properties
githubRepoOwner=YourGitHubUsername
githubRepoName=PokemonZACompanion
```

Rebuild the app after changing this.

### 2. Create the GitHub repository

```powershell
cd C:\Users\marti\PokemonZACompanion
git init
git add .
git commit -m "Initial commit: Pokemon ZA Companion"
gh auth login
gh repo create PokemonZACompanion --public --source=. --remote=origin --push
```

Or create the repo on [github.com/new](https://github.com/new), then:

```powershell
git remote add origin https://github.com/YOUR_USERNAME/PokemonZACompanion.git
git branch -M main
git push -u origin main
```

### 3. Publish an update (each new version)

1. Bump in `app/build.gradle.kts`:
   - `versionCode` (must increase, e.g. 16 → 17)
   - `versionName` (e.g. `"1.2.2"`)
2. Update `version.json` in the repo root with the **same** `versionCode` and `versionName`, plus `releaseNotes`.
3. Build APK: `.\gradlew.bat assembleRelease`
4. Create a GitHub Release and attach the APK:
   ```powershell
   git add .
   git commit -m "Release v1.2.2"
   git push
   git tag v1.2.2
   git push origin v1.2.2
   ```
   GitHub Actions (`.github/workflows/release.yml`) will build and attach `app-release.apk` when you push a `v*` tag.

   Or manually: **Releases → New release** → upload `app/build/outputs/apk/release/app-release.apk` as `PokemonZACompanion.apk`.

## How in-app updates work

1. On launch, the app reads `version.json` from your repo (`main` branch).
2. If `versionCode` is higher than the installed app, a dialog offers **Update** or **Not now**.
3. **Update** downloads the `.apk` from the latest GitHub Release and opens the Android installer.
4. Users may need to allow **Install unknown apps** for this app once (Settings).

## Build locally

Requirements: Android SDK, JDK 17.

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`

## Disclaimer

Unofficial fan app. Not affiliated with Nintendo, Game Freak, or The Pokémon Company.
