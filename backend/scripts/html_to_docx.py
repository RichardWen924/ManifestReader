#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
HTML to Docx Converter (using htmldocx)
Usage: python3 html_to_docx.py <input_html_file_path> <output_docx_path>
Output: JSON {"status": "success", "output": "..."}
"""
import sys
import json
from docx import Document
from htmldocx import HtmlToDocx

def convert_to_docx(html_file_path, output_docx_path):
    try:
        # Read HTML content from file
        with open(html_file_path, 'r', encoding='utf-8') as f:
            html_content = f.read()

        # Create new document
        document = Document()
        new_parser = HtmlToDocx()
        
        # Convert HTML to Docx
        new_parser.add_html_to_document(html_content, document)
        
        # Save document
        document.save(output_docx_path)
            
        print(json.dumps({
            "status": "success",
            "output": output_docx_path
        }))
    except Exception as e:
        print(json.dumps({
            "status": "error",
            "message": str(e)
        }))
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(json.dumps({"status": "error", "message": "Usage: python3 html_to_docx.py <input_html_file_path> <output_docx_path>"}))
        sys.exit(1)
    
    convert_to_docx(sys.argv[1], sys.argv[2])
