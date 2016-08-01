#! /bin/sh
### BEGIN INIT INFO
# Provides:          camerabox
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: CameraBox service script
# Description:       CameraBox service script
### END INIT INFO

# Author: Augustin Peyrard <augustin.peyrard@gmail.com>

# This is a script to start the camerabox application at the server's startup. It intends to be copied
# in /etc/init.d/camerabox. After that create a symbolic link
# in /etc/rc directory of the runlevel you want, or use distribution utilities to do that, like
# "update-rc.d camerabox start 20 2 3 4 5 . stop 20 0 1 6 ." (20 being the priority)

# Define CAMERABOX_HOME as the root installation of camerabox app (EDIT THIS LINE)
CAMERABOX_HOME=/opt/camerabox/srv

# After this line, nothing has to be changed

VERBOSE="no"

JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt

# PATH should only include /usr/* if it runs after the mountnfs.sh script
PATH=/sbin:/usr/sbin:/bin:/usr/bin
DESC="CameraBox application"
NAME=camerabox
PIDFILE=/var/run/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME

#
# Function that starts the daemon/service
#
do_start()
{
	$JAVA_HOME/bin/java -cp "$CAMERABOX_HOME/etc:$CAMERABOX_HOME/dist/classes:$CAMERABOX_HOME/dist/dependency/*" -Duser.dir=$CAMERABOX_HOME org.teutinc.pi.camerabox.AppServer 2>&1 > /dev/null &
    echo $! > $PIDFILE

    return 0
}

#
# Function that stops the daemon/service
#
do_stop()
{
	# Return
	#   0 if daemon has been stopped
	#   1 if daemon was already stopped
	#   2 if daemon could not be stopped
	#   other if a failure occurred
	cat $PIDFILE|xargs kill
	rm -f $PIDFILE

	return 0
}

do_status() {
    #Return
    #   0 if daemon is not started
	#   1 if daemon is already running

	current_proc=`ps aux|grep [j]ava|grep camerabox`
	[ -x "$current_proc" ] || return 1

	return 0
}

case "$1" in
  start)
	[ "$VERBOSE" != no ] && log_daemon_msg "Starting $DESC" "$NAME"
	do_start
	case "$?" in
		0|1) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  stop)
	[ "$VERBOSE" != no ] && log_daemon_msg "Stopping $DESC" "$NAME"
	do_stop
	case "$?" in
		0|1) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  status)
	do_status
	return $?
	;;
  restart|force-reload)
	#
	# If the "reload" option is implemented then remove the
	# 'force-reload' alias
	#
	log_daemon_msg "Restarting $DESC" "$NAME"
	do_stop
	case "$?" in
	  0|1)
		do_start
		case "$?" in
			0) log_end_msg 0 ;;
			1) log_end_msg 1 ;; # Old process is still running
			*) log_end_msg 1 ;; # Failed to start
		esac
		;;
	  *)
		# Failed to stop
		log_end_msg 1
		;;
	esac
	;;
  *)
	echo "Usage: $SCRIPTNAME {start|stop|status|restart|force-reload}" >&2
	exit 3
	;;
esac

:
