/**
 * Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.template.io;

import java.io.IOException;

/**
 * CharWriter
 */
public class CharWriter extends Writer {
	
	java.io.Writer out;
	char[] chars;
	
	boolean inUse;	// 支持 reentrant
	
	public CharWriter(int bufferSize) {
		this.chars = new char[bufferSize];
	}
	
	public CharWriter init(java.io.Writer writer) {
		inUse = true;
		this.out = writer;
		return this;
	}
	
	public void close() {
		inUse = false;
		out = null;
	}
	
	public boolean isInUse() {
		return inUse;
	}
	
	public void flush() throws IOException {
		out.flush();
	}
	
	public void write(String str, int offset, int len) throws IOException {
		int size;
		while (len > 0) {
			size = (len > chars.length ? chars.length : len);
			
			str.getChars(offset, offset + size, chars, 0);
			out.write(chars, 0, size);
			
			offset += size;
			len -= size;
		}
	}
	
	public void write(String str) throws IOException {
		write(str, 0, str.length());
	}
	
	public void write(StringBuilder stringBuilder, int offset, int len) throws IOException {
		int size;
		while (len > 0) {
			size = (len > chars.length ? chars.length : len);
			
			stringBuilder.getChars(offset, offset + size, chars, 0);
			out.write(chars, 0, size);
			
			offset += size;
			len -= size;
		}
	}
	
	public void write(StringBuilder stringBuilder) throws IOException {
		write(stringBuilder, 0, stringBuilder.length());
	}
	
	public void write(IWritable writable) throws IOException {
		char[] data = writable.getChars();
		out.write(data, 0, data.length);
	}
	
	public void write(int intValue) throws IOException {
		IntegerWriter.write(this, intValue);
	}
	
	public void write(long longValue) throws IOException {
		LongWriter.write(this, longValue);
	}
	
	public void write(double doubleValue) throws IOException {
		FloatingWriter.write(this, doubleValue);
	}
	
	public void write(float floatValue) throws IOException {
		FloatingWriter.write(this, floatValue);
	}
	
	private static final char[] TRUE_CHARS = "true".toCharArray();
	private static final char[] FALSE_CHARS = "false".toCharArray();
	
	public void write(boolean booleanValue) throws IOException {
		out.write(booleanValue ? TRUE_CHARS : FALSE_CHARS);
	}
}






