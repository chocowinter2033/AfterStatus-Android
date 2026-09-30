# Build AfterStatus APK from your Android phone (cloud build)

This project now includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`. It builds a debug APK on GitHub's build machine, so you do not need Android SDK/Gradle installed on your phone. You do need a GitHub account and must upload the project to a repository yourself; this package cannot create a repository or access your account.

## Steps

1. Download and extract `AfterStatus-Android-Cloud-Build.zip` on your phone.
2. In a browser, open https://github.com/new and create a repository named `AfterStatus-Android`. For the simplest setup, choose **Public** (GitHub Actions minutes/availability differ for private repositories). Do not add a README, license, or .gitignore because these are already included.
3. Open the new repository. Choose **Add file → Upload files** and upload the contents of the extracted `AfterStatus-Android` folder, including the hidden `.github/workflows/build-apk.yml` file. If the mobile website hides upload options, enable your browser's **Desktop site**. GitHub's browser uploader may not preserve hidden folders in some mobile workflows; if `.github/workflows/build-apk.yml` was not uploaded, create it in the repository using **Add file → Create new file**, enter the exact path `.github/workflows/build-apk.yml`, and paste the workflow from the included file.
4. Commit the files to the `main` branch.
5. In the repository, open **Actions**. Choose **Build AfterStatus APK**. If it did not start automatically, press **Run workflow** and confirm.
6. Wait for the run to finish. Open the successful run and find **Artifacts** near the bottom. Tap `AfterStatus-debug-APK` to download the artifact ZIP.
7. Extract that downloaded ZIP in the phone's Files app. It contains `app-debug.apk`.
8. Tap `app-debug.apk` to install it. Android may ask you to allow your browser or file manager to install unknown apps.

## Notes

- The artifact is a debug/testing APK, not a Play Store release.
- The source has not yet been verified by a successful build. If the workflow fails, open the run, expand the **Build debug APK** step, and share the first error block so the source can be fixed.
- Do not upload secrets or API keys into a public repository. This starter app does not require a secret to build.
- This workflow uses GitHub-hosted runners; it does not use your phone's battery for compiling. GitHub's usage limits and terms apply.
