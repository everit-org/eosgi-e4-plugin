#!/bin/sh
#
# Copyright (C) 2011 Everit Kft. (http://www.everit.org)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


## Install dist to m2 repo
#DIST_REPO=~/.m2/repository/org/everit/osgi/dev/dist/eosgi-dist-equinox_3.10.100
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
