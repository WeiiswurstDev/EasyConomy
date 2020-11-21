#!/bin/sh

# This shell script will be run everytime the plugin files are compiled. That is as long as it's referenced to run within the config.yml.
# Below this paragraph is a command to compress the generated backups (.dat is assumed - change it to what you currently have) into a zstd compressed tarball. It was only tested by me on Fedora 33 (so GNU/linux), 
# so I cannot gurantee that it would run anywhere else, which is why it's commented.

# tar --remove-files -I "zstd --ultra -22" -cvf "backup-$(date +"%Y_%m_%d_%I_%M_%p").tar.zst" *.dat

# Additionally you can perform more actions within this file to, for example ship the now compressed backups into a safe offsite location, but that's something you'll need to figure out yourself.
