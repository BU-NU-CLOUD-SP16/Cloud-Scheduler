
import subprocess


print "scp"
p = subprocess.Popen("scp ../script.sh ubuntu@129.10.3.91:~",shell=True)
p.wait()

print "chmod"
p = subprocess.Popen("ssh ubuntu@129.10.3.91 'chmod 777 script.sh'",shell=True)
p.wait()


print "execute"
p = subprocess.Popen("ssh -A ubuntu@129.10.3.91 './script.sh'",shell=True)
p.wait()


