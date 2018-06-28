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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Entity")
public class DirectoryEntity
{
	private String id;
	private String description;
	private Date createdAt;
	private Date updatedAt;
	private Long size;

	public DirectoryEntity(final File file)
	{
		id = file.getName();
		description = file.getName();
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
		return "{" +
				"\"id\":\"" + id + "\"," +
				"\"description\":\"" + description + "\"," +
				"\"createdAt\":\"" + format(createdAt) +
				"\"," + "\"updatedAt\":\"" + format(updatedAt) + "\"," +
				"\"size\":" + size + "" +
				"}";
	}

	@Override
	public String toString()
	{
		return toJSON();
	}

	private static String format(final Date date)
	{
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
	}
}
