#!/usr/bin/env bash

if [ -z $1 ] 
then
	echo "specify host where to deploy, as first argument!";
	exit -1;
fi 

deploy_srv=0;
deploy_ui=0;
deploy_admin=0;
deploy_button=0;
if [ -z $2 ]
then
	deploy_srv=1;
	deploy_ui=1;
	deploy_admin=1;
	deploy_button=1;
else
	if [[ $2 == *"srv"* ]]
	then
  	deploy_srv=1;
	fi
	if [[ $2 == *"ui"* ]]
	then
  	deploy_ui=1;
	fi
	if [[ $2 == *"admin"* ]]
	then
  	deploy_admin=1;
	fi
	if [[ $2 == *"button"* ]]
	then
  	deploy_button=1;
	fi
fi

dir=$(dirname $0)
current=$(pwd)

# cleanup
rm -rf $dir/dist
mkdir $dir/dist

# srv packaging and deployment
if [ $deploy_srv = 1 ]
then
	cd $dir/srv
	mvn compile dependency:copy-dependencies -DincludeScope=compile
	cd $current
	tar cfz $dir/dist/srv.tgz -C $dir/srv/target .
	scp $dir/dist/srv.tgz $1:/opt/camerabox
fi

# ui packaging and deployment
if [ $deploy_ui = 1 ]
then
	cd $dir/ui
	grunt build
	cd $current
	tar cfz $dir/dist/ui.tgz -C $dir/ui/dist .
	scp $dir/dist/ui.tgz $1:/opt/camerabox
fi

# admin packaging and deployment
if [ $deploy_admin = 1 ]
then
	cd $dir/admin-ui
	grunt build
	cd $current
	tar cfz $dir/dist/admin-ui.tgz -C $dir/admin-ui/dist .
	scp $dir/dist/admin-ui.tgz $1:/opt/camerabox
fi

# admin packaging and deployment
if [ $deploy_button = 1 ]
then
	cd $dir/button
	grunt build
	cd $current
	tar cfz $dir/dist/button.tgz -C $dir/button/dist .
	scp $dir/dist/button.tgz $1:/opt/camerabox
fi

# launch remote install
ssh $1 /opt/camerabox/install.sh $2

