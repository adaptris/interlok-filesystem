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
import java.text.NumberFormat;
import java.util.Locale;

import org.hibernate.validator.constraints.NotBlank;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * List the contents of a directory.
 *
 * @config directory-listing-service
 */
@AdapterComponent
@ComponentProfile(summary = "List the contents of a directory", tag = "service,list,directory,ls")
@XStreamAlias("directory-listing-service")
public class DirectoryListingService extends ServiceImp
{
	/**
	 * Whether debug mode is enabled.
	 */
	@InputFieldDefault(value = "false")
	@AdvancedConfig
	private Boolean debugMode;

	/**
	 * The metadata key to export file listing data to.
	 */
	@NotBlank
	private String metadataKey;

	/**
	 * The folder to get a directory listing of.
	 */
	@NotBlank
	@InputFieldHint(expression = true)
	private String directoryPath;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException
	{
		if (directoryPath == null)
		{
			log.error("Directory path is NULL, this service ({}) will not execute.", getUniqueId());
			throw new ServiceException("Missing Required Parameters");
		}

		NumberFormat.getNumberInstance(Locale.UK);
		try
		{
			final String path = message.resolve(getDirectoryPath());
			final File directory = new File(path);
			log.trace("ls: {} ", directory.getAbsolutePath());
			if (directory.exists() && directory.isDirectory())
			{
				final JSONArray entities = new JSONArray();
				for (final File file : directory.listFiles())
				{
					log.trace("Found file: {}", file.getName());
					
					DirectoryEntity entity = new DirectoryEntity(file);
					entities.put(new JSONObject(entity.toJSON()));
				}
				setOutput(message, entities.toString());
			}
			else
			{
				log.warn("Directory does not exist: {}", path);
				setOutput(message, "[]");
			}
		}
		catch (final JSONException e)
		{
			log.error(e.getMessage());
			throw new ServiceException(e);
		}
		catch (final InterlokException e)
		{
			log.error(e.getMessage());
			throw new ServiceException(e);
		}
	}

	/**
	 * Depending on whether the metadata key is set, put the directory listing output in the correct place (metadata or payload).
	 * 
	 * @param message
	 *            The message.
	 * @param output
	 *            The directory listing.
	 * 
	 * @throws InterlokException
	 *             Thrown if there is a problem with the metadata key.
	 */
	private void setOutput(final AdaptrisMessage message, final String output) throws InterlokException
	{
		if (metadataKey == null)
		{
			message.setContent(output, message.getContentEncoding());
		}
		else
		{
			message.addMessageHeader(metadataKey, output);
		}
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void prepare() throws CoreException
	{
		/* empty method */
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void closeService()
	{
		/* empty method */
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void initService() throws CoreException
	{
		if (directoryPath == null)
		{
			log.warn("Directory path is NULL, this service ({}) will not execute.", getUniqueId());
		}
	}

	/**
	 * Get the directory path parameter.
	 *
	 * @return The directory path parameter.
	 */
	public String getDirectoryPath()
	{
		return directoryPath;
	}

	/**
	 * Set the directory path parameter.
	 *
	 * @param directoryPath
	 *            The directory path parameter.
	 */
	public void setDirectoryPath(final String directoryPath)
	{
		this.directoryPath = directoryPath;
	}

	/**
	 * Get the metadata key parameter.
	 *
	 * @return The metadata key parameter.
	 */
	public String getMetadataKey()
	{
		return metadataKey;
	}

	/**
	 * Set the metadata key parameter.
	 *
	 * @param metadataKey
	 *            The metadata key parameter.
	 */
	public void setMetadataKey(String metadataKey)
	{
		this.metadataKey = metadataKey;
	}

	/**
	 * Check whether debug mode is enabled.
	 *
	 * @return True if debug mode is enabled.
	 */
	protected boolean debugMode()
	{
		return getDebugMode() != null ? getDebugMode().booleanValue() : false;
	}

	/**
	 * Check whether debug mode is enabled.
	 *
	 * @return True if debug mode is enabled.
	 */
	public Boolean getDebugMode()
	{
		return debugMode;
	}

	/**
	 * Enable/Disable debug mode.
	 *
	 * @param debugMode
	 *            Whether debug mode should be enabled.
	 */
	public void setDebugMode(final Boolean debugMode)
	{
		this.debugMode = debugMode;
	}

	/**
	 * Get a human readable file size.
	 *
	 * @param bytes
	 *            The file size in bytes.
	 *
	 * @return A String representation of the file size.
	 */
	private static String humanReadableByteCount(final long bytes)
	{
		final int k = 1024;
		if (bytes < k)
		{
			return bytes + " B";
		}
		final int exp = (int)(Math.log(bytes) / Math.log(k));
		return String.format("%.1f %ciB", bytes / Math.pow(k, exp), "KMGTPE".charAt(exp - 1));
	}
}