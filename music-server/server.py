import os
from http.server import HTTPServer, SimpleHTTPRequestHandler

class CustomHTTPRequestHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        self.base_path = "/app/music"
        super().__init__(*args, **kwargs)

    def translate_path(self, path):
        path = super().translate_path(path)
        relpath = os.path.relpath(path, os.getcwd())
        fullpath = os.path.join(self.base_path, relpath)
        return fullpath

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", ["localhost:5173", "localhost:8080"],)
        super().end_headers()

    def do_GET(self):
        print(f"Requested path: {self.path}")
        super().do_GET()

def run(server_class=HTTPServer, handler_class=CustomHTTPRequestHandler):
    server_address = ("0.0.0.0", 8000)
    httpd = server_class(server_address, handler_class)
    print(f"Serving files from /app/music on http://0.0.0.0:8000")
    httpd.serve_forever()

if __name__ == "__main__":
    run()