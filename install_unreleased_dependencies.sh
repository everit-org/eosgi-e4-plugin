#!/bin/sh

## Install dist to m2 repo
#DIST_REPO=~/.m2/repository/org/everit/osgi/dev/dist2/eosgi-dist-equinox_3.10.100
#mkdir -p $DIST_REPO
#cp ./eosgi-dist-equinox_3.10.100-v201510210808.zip $DIST_REPO/
#cd $DIST_REPO
#unzip eosgi-dist-equinox_3.10.100-v201510210808.zip

## install dist scheam to m2 repo
mkdir ~/git
cd ~/git
git clone https://github.com/everit-org/eosgi-dist-schema.git
cd eosgi-dist-schema
mvn clean install
