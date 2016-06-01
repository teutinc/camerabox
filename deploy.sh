#!/usr/bin/env bash

if [ -z $1 ] 
then
	echo "specify host where to deploy, as first argument!"
	exit -1
fi 

dir=/opt/dev/java/photobooth/prod/photobooth
current=$(pwd)

# srv packaging
rm -f $dir/srv.tgz
cd $dir/srv
mvn compile dependency:copy-dependencies -DincludeScope=compile
cd $current
tar cfz $dir/srv.tgz -C $dir/srv/target .

# ui packaging
rm -f $dir/ui.tgz $dir/admin-ui.tgz
tar cfz $dir/ui.tgz -C $dir/ui/dist .
tar cfz $dir/admin-ui.tgz -C $dir/admin-ui/dist .

# deployment of packages 
scp $dir/srv.tgz $1:/opt/camerabox
scp $dir/ui.tgz $1:/opt/camerabox
scp $dir/admin-ui.tgz $1:/opt/camerabox

# launch remote install
ssh $1 /opt/camerabox/install.sh

