"""
LDREAMS Complete Setup Script
Run this to initialize git, download Gradle wrapper, and prepare for build.
"""
import os
import subprocess
import zipfile
import json
import urllib.request
import tempfile
import shutil
from pathlib import Path

BASE_DIR = Path(__file__).parent.resolve()

def run(cmd, cwd=None):
    print(f"Running: {' '.join(cmd)}")
    result = subprocess.run(cmd, cwd=cwd or BASE_DIR, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"  stderr: {result.stderr[:200]}")
    else:
        print(f"  stdout: {result.stdout[:200]}")
    return result

def download_gradle_wrapper():
    """Download the gradle-wrapper.jar."""
    jar_path = BASE_DIR / "gradle" / "wrapper" / "gradle-wrapper.jar"
    if jar_path.exists():
        print("gradle-wrapper.jar already exists")
        return True

    # Try downloading from Gradle releases
    urls = [
        "https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar",
        "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar",
    ]

    for url in urls:
        try:
            print(f"Downloading gradle-wrapper.jar from {url}...")
            urllib.request.urlretrieve(url, jar_path)
            if jar_path.exists() and jar_path.stat().st_size > 1000:
                print(f"  Success! ({jar_path.stat().st_size} bytes)")
                return True
        except Exception as e:
            print(f"  Failed: {e}")

    # Alternative: download full Gradle distribution and extract
    print("Trying alternative: download Gradle distribution...")
    gradle_url = "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
    zip_path = BASE_DIR / "gradle.zip"
    try:
        urllib.request.urlretrieve(gradle_url, zip_path)
        extract_dir = BASE_DIR / "gradle-extract"
        with zipfile.ZipFile(zip_path, 'r') as zf:
            zf.extractall(extract_dir)
        # Find and copy the wrapper jar
        for root, dirs, files in os.walk(extract_dir):
            for f in files:
                if f == "gradle-wrapper.jar":
                    src = Path(root) / f
                    shutil.copy2(src, jar_path)
                    print(f"  Extracted gradle-wrapper.jar ({jar_path.stat().st_size} bytes)")
                    break
        shutil.rmtree(extract_dir, ignore_errors=True)
        zip_path.unlink(missing_ok=True)
        return jar_path.exists()
    except Exception as e:
        print(f"  Failed: {e}")
        return False

def setup_git():
    """Initialize git repo."""
    if (BASE_DIR / ".git").exists():
        print("Git repo already initialized")
        return True

    result = run(["git", "init"])
    if result.returncode == 0:
        run(["git", "checkout", "-b", "main"])
        return True
    return False

def commit_files():
    """Add and commit all files."""
    run(["git", "add", "-A"])
    result = run(["git", "commit", "-m", "Initial commit: LDREAMS lucid dreaming Android app"])
    return result.returncode == 0

def find_android_sdk():
    """Find Android SDK path and create local.properties."""
    possible_paths = [
        Path(os.environ.get("ANDROID_HOME", "")),
        Path(os.environ.get("ANDROID_SDK_ROOT", "")),
        Path(os.environ.get("LOCALAPPDATA", "")) / "Android" / "Sdk",
        Path.home() / "AppData" / "Local" / "Android" / "Sdk",
        Path("C:/Android/Sdk"),
        Path("C:/Program Files/Android/Sdk"),
    ]

    for path in possible_paths:
        if path.exists() and (path / "platforms").exists():
            print(f"Found Android SDK at: {path}")
            with open(BASE_DIR / "local.properties", "w") as f:
                f.write(f"sdk.dir={path.as_posix()}\n")
            return True

    print("Android SDK not found locally.")
    print("The GitHub Actions workflow will build the APK in CI.")
    return False

def setup_github():
    """Check GitHub CLI and provide instructions."""
    result = run(["gh", "auth", "status"])
    if result.returncode == 0:
        print("GitHub CLI is authenticated!")
        # Create repo
        result = run(["gh", "repo", "create", "LDREAMS", "--public", "--source=.", "--remote=origin", "--push"])
        if result.returncode == 0:
            print("GitHub repo created and code pushed!")
            return True
    else:
        print("GitHub CLI not authenticated.")
        print("\nTo push to GitHub manually:")
        print("  1. Create repo at https://github.com/new")
        print("  2. Run:")
        print("     git remote add origin https://github.com/YOUR_USERNAME/LDREAMS.git")
        print("     git push -u origin main")
    return False

def main():
    print("=" * 60)
    print("  LDREAMS - Complete Setup")
    print("=" * 60)

    print("\n[1/5] Setting up git...")
    setup_git()

    print("\n[2/5] Committing files...")
    commit_files()

    print("\n[3/5] Downloading Gradle wrapper...")
    download_gradle_wrapper()

    print("\n[4/5] Finding Android SDK...")
    find_android_sdk()

    print("\n[5/5] Setting up GitHub...")
    setup_github()

    print("\n" + "=" * 60)
    print("  Setup complete!")
    print()
    print("  To build APK locally:")
    print("    - Install Android SDK (if not installed)")
    print("    - Run: gradlew.bat assembleDebug")
    print()
    print("  Or let GitHub Actions build it:")
    print("    - Push to GitHub")
    print("    - Go to Actions tab")
    print("    - The workflow will build and upload the APK")
    print("=" * 60)

if __name__ == "__main__":
    main()
