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
import java.io.FileNotFoundException;
import java.io.IOException;
import jakarta.validation.constraints.NotBlank;
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
 * Stat a file in a directory.
 *
 * @config directory-entity-service
 */
@AdapterComponent
@ComponentProfile(summary = "Get various file metadata info", tag = "service,directory,stat,file")
@XStreamAlias("directory-entity-service")
public class DirectoryEntityService extends ServiceImp
{
	/**
	 * Whether debug mode is enabled.
	 */
	@InputFieldDefault(value = "false")
	@AdvancedConfig
	private Boolean debugMode;

	/**
	 * The metadata key to export file data to.
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

		try
		{
			final String path = message.resolve(getDirectoryPath());
			final File file = new File(path);
			if (!file.exists())
			{
				throw new FileNotFoundException("File not found : " + path);
			}
			log.trace("Found file: {} ", file.getAbsolutePath());

			final DirectoryEntity entity = new DirectoryEntity(file);
			final JSONObject json = new JSONObject(entity.toJSON());
			setOutput(message, json.toString());
		}
		catch (final IOException e)
		{
			log.error(e.getMessage());
			throw new ServiceException(e);
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
}
