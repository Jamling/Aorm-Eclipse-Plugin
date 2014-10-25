/*
 * Copyright 2012 Jamling(li.jamling@gmail.com).
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
package cn.ieclipse.aorm.eclipse.helpers;

/**
 * @author melord
 * 
 */
public class ComponentAttribute {
	public static final int TYPE_LIST = 0;
	public static final int TYPE_MLIST = 1;
	public static final int TYPE_STRING = 2;
	public static final int TYPE_STYPE = 3;
	public static final int TYPE_DRAWABLE = 4;
	public static final int TYPE_STRING_REF = 5;

	private String name;
	private String value;
	private int type;
	private String[] formats;

	public String getName() {
		return name;
	}

	public String getShortName() {
		if (name != null) {
			if (name.startsWith("android:")) {
				return name.substring("android:".length());
			}
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setFormats(String options) {
		String trimStr = options.replaceAll("\\[", "").replaceAll("\\]", "")
				.replaceAll("\"", "").trim();
		String[] ret = null;

		if (trimStr.indexOf('|') > 0) {
			type = TYPE_LIST;
			String[] src = trimStr.split("\\|");
			ret = new String[src.length];
			for (int i = 0; i < src.length; i++) {
				ret[i] = src[i].trim();
			}
		} else if (trimStr.indexOf(',') > 0) {
			type = TYPE_MLIST;
			String[] src = trimStr.split(",");
			ret = new String[src.length];
			for (int i = 0; i < src.length; i++) {
				ret[i] = src[i].trim();
			}
		} else if ("string".equals(trimStr)) {
			type = TYPE_STRING;
		} else if ("drawable resource".equals(trimStr)) {
			type = TYPE_DRAWABLE;
		} else if ("string resource".equals(trimStr)) {
			type = TYPE_STRING_REF;
		} else if ("resource or theme".equals(trimStr)) {
			type = TYPE_STYPE;
		} else{
			type = TYPE_STRING;
		}
		this.formats = ret;
	}

	public String[] getFormats() {
		return formats;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComponentAttribute) {
			if (name == null) {
				if (obj == null) {
					return true;
				}
			} else {
				return name.equals(((ComponentAttribute) obj).getName());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append('=');
		if (formats != null) {
			for (String opt : formats) {
				sb.append(opt);
				if (type == TYPE_LIST) {
					sb.append("|");
				} else {
					sb.append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
		} else {
			sb.append(value);
		}
		return sb.toString();
	}
}
