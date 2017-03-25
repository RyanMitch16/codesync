import subprocess
import socket
import sublime
import sublime_plugin



class Test(sublime_plugin.ViewEventListener):
    def on_modified_async(self):
        # create a socket object
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 

        # get local machine name
        host = socket.gethostname()                           

        port = 9999

        # connection to hostname on the port.
        s.connect((host, port))                               

        # Receive no more than 1024 bytes
        #msg = s.recv(1024)
        msg = 'connected' + "\r\n"
        s.send(msg.encode('ascii'))                        

        s.close()

        #print (msg.decode('ascii'))

        #subprocess.Popen(["/home/chris/t.sh"])
        #subprocess.Popen(["touch", "/home/chris/hi"])


