#!/bin/bash

# --- Configuration ---
INSTALL_ROOT="$HOME/.local/share/trenovac-maturity"
ICON_DIR="$HOME/.local/share/icons"
DESKTOP_DIR="$HOME/.local/share/applications"
REPO="PanJohnny/trenovac-maturity"

# --- 1. Fetch Latest Release Info ---
echo "Checking for latest release..."
RESPONSE=$(curl -fsSL "https://api.github.com/repos/$REPO/releases/latest")
TAG_NAME=$(echo "$RESPONSE" | grep -Po '"tag_name": "\K.*?(?=")')
DOWNLOAD_URL=$(echo "$RESPONSE" | grep -Po '"browser_download_url": "\K.*?(?=TrenovacMaturity-linux-x64.tar.gz)"')TrenovacMaturity-linux-x64.tar.gz

if [ -z "$TAG_NAME" ]; then
  echo "Error: Could not find latest release."
  exit 1
fi

# --- 2. Update Logic ---
if [ -d "$INSTALL_ROOT/$TAG_NAME" ]; then
  echo "You are already on the latest version ($TAG_NAME)."
else
  echo "Updating to $TAG_NAME..."

  # Create temp dir for download
  TMP_DIR=$(mktemp -d)
  curl -L -o "$TMP_DIR/app.tar.gz" "$DOWNLOAD_URL"

  # Clean old versions
  rm -rf "$INSTALL_ROOT"
  mkdir -p "$INSTALL_ROOT/$TAG_NAME"

  # Extract (Strip components if the tar contains a root folder)
  tar -xzf "$TMP_DIR/app.tar.gz" -C "$INSTALL_ROOT/$TAG_NAME" --strip-components=1
  rm -rf "$TMP_DIR"

  echo "Installation successful."
fi

# --- 3. Desktop Entry & Icon ---
# Note: JPackage image usually has the icon inside bin/ or lib/
# We download the source icon to be safe.
mkdir -p "$ICON_DIR"
ICON_PATH="$ICON_DIR/trenovac-maturity.png"
if [ ! -f "$ICON_PATH" ]; then
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

# --- 4. Launch ---
read -p "Launch now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  "$INSTALL_ROOT/$TAG_NAME/bin/TrenovacMaturity" &
fi

echo "Done!"