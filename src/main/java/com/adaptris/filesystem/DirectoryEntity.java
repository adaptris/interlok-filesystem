/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.filesystem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.adaptris.core.services.metadata.DateFormatBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.json.JSONObject;

@XStreamAlias("Entity")
public class DirectoryEntity
{
	private String id;
	private String description;
	private String absolutePath;
	private String parentDirectory;
	private Date createdAt;
	private Date updatedAt;
	private Long size;

	public DirectoryEntity(final File file)
	{
		id = file.getName();
		description = file.getName();
		absolutePath = file.getAbsolutePath();
		parentDirectory = file.getParentFile().getName();
		createdAt = new Date(file.lastModified());
		updatedAt = new Date(file.lastModified());
		size = file.length();
	}

	public String getId()
	{
		return id;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getParentDirectory() {
		return parentDirectory;
	}

	public void setParentDirectory(String parentDirectory) {
		this.parentDirectory = parentDirectory;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(final Date createdAt)
	{
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(final Date updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public Long getSize()
	{
		return size;
	}

	public void setSize(final Long size)
	{
		this.size = size;
	}

	public String toJSON()
	{
		return toJSONObject().toString();
	}

	private JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", id);
		jsonObject.put("absolutePath", absolutePath);
		jsonObject.put("parentDirectory", parentDirectory);
		jsonObject.put("createdAt", format(createdAt));
		jsonObject.put("updatedAt", format(updatedAt));
		jsonObject.put("size", size);
		return jsonObject;
	}

	JSONObject toJSONObject(DateFormatBuilder.DateFormatter dateFormatter) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", id);
		jsonObject.put("absolutePath", absolutePath);
		jsonObject.put("parentDirectory", parentDirectory);
		jsonObject.put("createdAt", dateFormatter.toString(createdAt));
		jsonObject.put("updatedAt", dateFormatter.toString(updatedAt));
		jsonObject.put("size", size);
		return jsonObject;
	}

	@Override
	public String toString()
	{
		return toJSON();
	}

	private String format(final Date date)
	{
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
	}
}
