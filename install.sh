#!/usr/bin/env bash

install_srv=0;
install_ui=0;
install_admin=0;
if [ -z $2 ]
then
	install_srv=1;
	install_ui=1;
	install_admin=1;
else
	if [[ $2 == *"srv"* ]]
	then
  	install_srv=1;
	fi
	if [[ $2 == *"ui"* ]]
	then
  	install_ui=1;
	fi
	if [[ $2 == *"admin"* ]]
	then
  	install_admin=1;
	fi
fi

dir=$(dirname $0)

# install srv
if [ $install_srv = 1 ]
then
    if [ -d "$dir/srv/dist" ]; then rm -rf $dir/srv/dist; fi
    mkdir -p $dir/srv/dist
    tar xfz $dir/srv.tgz -C $dir/srv/dist
fi

# install ui
if [ $install_ui = 1 ]
then
    if [ -d "$dir/ui" ]; then rm -rf $dir/ui; fi
    mkdir $dir/ui
    tar xfz $dir/ui.tgz -C $dir/ui
fi

# install admin
if [ $install_admin = 1 ]
then
    if [ -d "$dir/admin-ui" ]; then rm -rf $dir/admin-ui; fi
    mkdir $dir/admin-ui
    tar xfz $dir/admin-ui.tgz -C $dir/admin-ui
fi

 
