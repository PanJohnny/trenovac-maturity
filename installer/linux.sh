#!/bin/bash

# --- Configuration ---
INSTALL_ROOT="$HOME/.local/share/trenovac-maturity"
ICON_DIR="$HOME/.local/share/icons"
DESKTOP_DIR="$HOME/.local/share/applications"
REPO="PanJohnny/trenovac-maturity"
ASSET_NAME="TrenovacMaturity-linux-x64.tar.gz"

# --- 1. Fetch Latest Release Info ---
echo "Checking for latest release..."
# Get the JSON, then find the specific browser_download_url for our tar.gz
RESPONSE=$(curl -fsSL "https://api.github.com/repos/$REPO/releases/latest")

# Robust way to grab the URL for the linux tar.gz
DOWNLOAD_URL=$(echo "$RESPONSE" | grep "browser_download_url" | grep "$ASSET_NAME" | cut -d '"' -f 4)
TAG_NAME=$(echo "$RESPONSE" | grep -m 1 "tag_name" | cut -d '"' -f 4)

if [ -z "$DOWNLOAD_URL" ]; then
  echo "Error: Could not find the download URL for $ASSET_NAME."
  echo "Make sure the file is uploaded to the GitHub release exactly as named."
  exit 1
fi

# --- 2. Update Logic ---
if [ -d "$INSTALL_ROOT/$TAG_NAME" ]; then
  echo "You are already on the latest version ($TAG_NAME)."
else
  echo "Updating to $TAG_NAME..."

  TMP_DIR=$(mktemp -d)
  echo "Downloading..."
  if ! curl -L -o "$TMP_DIR/app.tar.gz" "$DOWNLOAD_URL"; then
    echo "Error: Download failed."
    rm -rf "$TMP_DIR"
    exit 1
  fi

  # 1. Ensure the parent directory exists
  mkdir -p "$INSTALL_ROOT"

  # 2. Remove ONLY the specific version folder if it somehow existed partially
  rm -rf "$INSTALL_ROOT/$TAG_NAME"

  # 3. Create the specific version folder explicitly
  mkdir -p "$INSTALL_ROOT/$TAG_NAME"

  # 4. Extract
  echo "Extracting..."
  if ! tar -xzf "$TMP_DIR/app.tar.gz" -C "$INSTALL_ROOT/$TAG_NAME" --strip-components=1; then
    echo "Error: Extraction failed."
    exit 1
  fi

  rm -rf "$TMP_DIR"
  echo "Installation successful."
fi

# --- 3. Desktop Entry & Icon ---
mkdir -p "$ICON_DIR"
ICON_PATH="$ICON_DIR/trenovac-maturity.png"
if [ ! -f "$ICON_PATH" ]; then
    echo "Downloading icon..."
    curl -fsSL -o "$ICON_PATH" "https://raw.githubusercontent.com/$REPO/master/src/main/resources/me/panjohnny/trenovacmaturity/icon.png"
fi

echo "Setting up desktop entry..."
cat > "$DESKTOP_DIR/trenovac-maturity.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=Trénovač maturity
Comment=Nástroj pro přípravu na maturitu
Exec="$INSTALL_ROOT/$TAG_NAME/bin/TrenovacMaturity"
Icon=$ICON_PATH
Terminal=false
Categories=Education;
StartupNotify=true
EOF

chmod +x "$DESKTOP_DIR/trenovac-maturity.desktop"
echo "Done! App installed to $INSTALL_ROOT/$TAG_NAME"

# --- 4. Launch ---
read -p "Launch now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  "$INSTALL_ROOT/$TAG_NAME/bin/TrenovacMaturity" &
fi