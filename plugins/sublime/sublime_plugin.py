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
        if(view.is_dirty()):
            try:
                filename = view.file_name()
                call = "UPDATE_OTHERS:"
                msg = intToBytes(len(call))
                msg += bytes(call, 'utf_8') 
                msg += intToBytes(len(filename))
                msg += bytes(filename, 'utf_8')
                msg += intToBytes(len(view.substr(sublime.Region(0, view.size()))))
                msg += bytes(view.substr(sublime.Region(0, view.size())), 'utf_8')
                try: 
                    s.connect((host, port))
                    s.send(msg)
                    s.close()
                    #sublime.save_settings(filename)
                except ConnectionRefusedError:
                    print("Connection Refused")
            except TypeError:
                print("Type Error")


        #print(view.substr(sublime.Region(0, view.size()-1)

        
def intToBytes(num):
    return bytes([(num >> 24) & 255, (num >> 16) & 255, (num >> 8) & 255, (num >> 0) & 255])
    #pad = 4 - len(arr)
    #return bytes(pad) + arr 
