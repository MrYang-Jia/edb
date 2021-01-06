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

package com.jfinal.kit;

import java.io.File;

/**
 * FileKit.
 */
public class FileKit {
	public static void delete(File file) {
		if (file != null && file.exists()) {
			if (file.isFile()) {
				file.delete();
			}
			else if (file.isDirectory()) {
				File files[] = file.listFiles();
				if (files != null) {
					for (int i=0; i<files.length; i++) {
						delete(files[i]);
					}
				}
				file.delete();
			}
		}
	}
	
	public static String getFileExtension(String fileFullName) {
	    if (StrKit.isBlank(fileFullName)) {
            throw new RuntimeException("fileFullName is empty");
        }
	    return  getFileExtension(new File(fileFullName));
	}
	
	public static String getFileExtension(File file) {
	    if (null == file) {
	        throw new NullPointerException();
	    }
	    String fileName = file.getName();
	    int dotIdx = fileName.lastIndexOf('.');
	    return (dotIdx == -1) ? "" : fileName.substring(dotIdx + 1);
    }
}
