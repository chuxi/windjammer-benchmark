#!/usr/bin/env python

from fabric.api import *
from fabric.colors import *

env.user = "wjm"

env.roledefs = {
    "benchmark": [ "wj-02" ]
}

env.use_ssh_config = True

local_dir = ""
remote_dir = "~/svr/benchmark-wjm"

def putfile(s, d):
    with settings(warn_only=True):
        result = put(s, d)
    if result.failed and not confirm("put tar file failed, Continue[Y/N]"):
        abort("aborting file put: %s-----%s" % (s, d))
    else:
        print green("Successfully put " + s + " to dir " + d)


def checkmd5(s, d):
    with settings(warn_only=True):
        lmd5 = local("md5 %s" % s, capture=True).split(' ')[-1]
        rmd5 = run("md5sum %s" % d).split(' ')[0]
    if lmd5 == rmd5:
        return True
    else:
        return False

def putJar(name):
    with cd(remote_dir + "/jars"):
        if checkmd5("target/" + name, remote_dir + "/jars/" + name) == False:
            putfile("target/" + name, remote_dir + "/jars/")

def deployConfig(upload_files):
    with settings(warn_only=True):
        if run("test -d %s" % remote_dir).failed:
            run("mkdir -p %s/{bin,conf,jars,logs,lib}" % remote_dir)
    with cd(remote_dir):
        for f in upload_files:
            if checkmd5(f, remote_dir + '/' + f) == False:
                putfile(f, remote_dir + '/' + f)
        run("chmod +x bin/*.sh")

@task
@roles("benchmark")
def deploy():
    # local("mvn clean package -DskipTests")
    putJar("bench-cache-1.0-SNAPSHOT.jar")

    upload_files = [
        "bin/benchmark.sh",
    ]
    deployConfig(upload_files)