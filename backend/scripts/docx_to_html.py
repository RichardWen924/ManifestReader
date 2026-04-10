#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Docx to HTML Converter (using mammoth)
Usage: python3 docx_to_html.py <input_docx_path>
Output: JSON {"status": "success", "html": "..."}
"""
import sys
import json
import mammoth

def convert_to_html(docx_path):
    try:
        with open(docx_path, "rb") as docx_file:
            result = mammoth.convert_to_html(docx_file)
            html = result.value # The generated HTML
            messages = result.messages # Any messages, such as warnings during conversion
            
        print(json.dumps({
            "status": "success",
            "html": html,
            "messages": [m.message for m in messages]
        }))
    except Exception as e:
        print(json.dumps({
            "status": "error",
            "message": str(e)
        }))
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"status": "error", "message": "Usage: python3 docx_to_html.py <input_docx_path>"}))
        sys.exit(1)
    
    convert_to_html(sys.argv[1])
