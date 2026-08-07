# PR: Fullscreen & Monitoring persistence

This PR applies the following changes:

- Enable immersive fullscreen mode using WindowInsetsControllerCompat in MainActivity.
- Persist Monitoring state across screens using MonitoringManager singleton (Flows + coroutine loop).
- Improve Home screen UI spacing, banner, and cards (no functional changes).
- Separate image assets (launcher adaptive icon, login logo, home banner) and add placeholder vector drawables.
- Fix vector drawable issues and coroutine usage for logout.
- Add GitHub Actions workflow to build debug APK and set JAVA_TOOL_OPTIONS headless for CI.

Validation steps:
1. Merge this PR to main.
2. Run the Build APK workflow on main (Actions -> Build APK -> Run workflow).
3. Download artifact and install on device. Verify fullscreen, login, monitoring persistence, and launcher icon.

Notes:
- No networking/backend code was modified.
- If you prefer to revert specific files instead of this PR, please review "Files changed" and let me know.
