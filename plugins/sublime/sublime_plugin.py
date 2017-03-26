import socket
import sublime
import sublime_plugin



class MonitorChanges(sublime_plugin.EventListener):
    def on_modified_async(self, view):
        # create a socket object
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 

        # get local machine name
        host = socket.gethostname()                           
        port = 9999

        # connection to hostname on the port.
        try:
            filename = view.file_name()
            call="UPDATE_OTHER"
            msg = str(len(call))+ call + str(len(filename))+filename+str(view.size()) + view.substr(sublime.Region(0, view.size()-1))
            try: 
                s.connect((host, port))
                s.send(msg.encode('utf_8'))
                s.close()
            except ConnectionRefusedError:
                pass
        except TypeError:
            pass


        #print(view.substr(sublime.Region(0, view.size()-1)

        
