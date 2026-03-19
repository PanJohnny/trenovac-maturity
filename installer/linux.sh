#!/bin/bash
# --- Configuration ---
INSTALL_DIR="$HOME/.local/share/trenovac-maturity"
ICON_DIR="$HOME/.local/share/icons"
DESKTOP_DIR="$HOME/.local/share/applications"
REPO="PanJohnny/trenovac-maturity"

# --- 1. Dependency Check (Java 25+) ---
echo "Checking dependencies..."
if ! command -v java &> /dev/null; then
  echo "Error: Java is not installed. Please install Java 25 or higher."
  exit 1
fi

JAVA_VERSION_FULL=$(java -version 2>&1 | awk -F '"' 'NR==1{print $2}')
JAVA_MAJOR="$(printf '%s' "$JAVA_VERSION_FULL" | sed -E 's/^1\.([0-9]+).*/\1/; s/^([0-9]+).*/\1/')"

if [[ "$JAVA_MAJOR" -lt 25 ]]; then
  echo "Error: Java 25 or higher is required. Current version: $JAVA_VERSION_FULL"
  exit 1
fi

# --- 2. Fetch Latest Release Info ---
echo "Fetching latest release info from GitHub..."
RESPONSE="$(curl -fsSL \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "User-Agent: Trenovac-Maturity-Installer" \
  https://api.github.com/repos/$REPO/releases/latest)" || {
  echo "Failed to connect to GitHub API."
  exit 1
}

TAG_NAME=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('tag_name', ''))")
DOWNLOAD_URL=$(echo "$RESPONSE" | python3 -c "
import sys, json
data = json.load(sys.stdin)
assets = data.get('assets', [])
links = [a['browser_download_url'] for a in assets if a['name'].endswith('.jar')]
print(links[0] if links else '')
")

if [ -z "$TAG_NAME" ] || [ -z "$DOWNLOAD_URL" ]; then
  echo "Error: Could not find a valid .jar release for tag: $TAG_NAME"
  exit 1
fi

NEW_FILENAME=$(basename "$DOWNLOAD_URL")
mkdir -p "$INSTALL_DIR"

# --- 3. Smart Update Logic ---
# Check if the specific latest version is already here
if [ -f "$INSTALL_DIR/$NEW_FILENAME" ]; then
  echo "You are already on the latest version ($TAG_NAME)."
else
  echo "New version detected: $TAG_NAME"

  # Clean up any OLD jar files to prevent the folder from filling up
  echo "Removing old versions..."
  rm -f "$INSTALL_DIR"/*.jar

  echo "Downloading $NEW_FILENAME..."
  if curl -L -o "$INSTALL_DIR/$NEW_FILENAME" "$DOWNLOAD_URL"; then
    echo "Update successful."
  else
    echo "Download failed."
    exit 1
  fi
fi

# --- 4. Desktop Entry & Icon ---
read -p "Update/Create desktop entry? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  mkdir -p "$ICON_DIR"
  ICON_PATH="$ICON_DIR/trenovac-maturity.png"
  ICON_URL="https://raw.githubusercontent.com/PanJohnny/trenovac-maturity/master/src/main/resources/me/panjohnny/trenovacmaturity/icon.png"

  if [ ! -f "$ICON_PATH" ]; then
    echo "Downloading icon..."
    curl -fsSL -o "$ICON_PATH" "$ICON_URL" || ICON_PATH="java"
  fi

  echo "Creating desktop entry..."
  cat > "$DESKTOP_DIR/trenovac-maturity.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=Trénovač maturity
Comment=Nástroj pro přípravu na maturitu
Exec=java -jar "$INSTALL_DIR/$NEW_FILENAME"
Icon=$ICON_PATH
Terminal=false
Categories=Education;
StartupNotify=true
EOF
  chmod +x "$DESKTOP_DIR/trenovac-maturity.desktop"
  echo "Desktop entry updated to point to $NEW_FILENAME"
fi

# --- 5. Launch ---
read -p "Launch Trenovac Maturity now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  java -jar "$INSTALL_DIR/$NEW_FILENAME" &
  exit 0
fi

echo "Done! You can find the app in your application menu."