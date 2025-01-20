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

import java.io.IOException;
import java.io.OutputStream;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Zip a directory and put the compressed data in the payload.
 *
 * @config zip-service
 */
@AdapterComponent
@ComponentProfile(summary = "Zip the contents of the message", tag = "service,zip")
@XStreamAlias("zip-service")
public class ZipService extends ServiceImp
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ZipService.class);

	/**
	 * The directory to zip.
	 */
	@InputFieldHint(expression = true)
	@NotBlank
	private String directoryPath;

	public ZipService()
	{
		/* empty, default constructor */
	}

	public ZipService(final String path)
	{
		setDirectoryPath(path);
	}

	/**
	 * Get the directory to zip.
	 *
	 * @return The directory to zip.
	 */
	public String getDirectoryPath()
	{
		return directoryPath;
	}

	/**
	 * Set the directory to zip.
	 *
	 * @param directoryPath
	 *            The directory to zip.
	 */
	public void setDirectoryPath(final String directoryPath)
	{
		this.directoryPath = Args.notBlank(directoryPath, "directoryPath");
		LOGGER.debug("Setting directory path to " + this.directoryPath);
	}

	@Override
	public void doService(final AdaptrisMessage msg) throws ServiceException
	{
		String p = msg.resolve(directoryPath);
		LOGGER.info("Target folder : " + p);
		final ZipFolder zipFolder = new ZipFolder(p);
		msg.setContent(null, null);
		try (final OutputStream outputStream = msg.getOutputStream())
		{
			final byte[] zippedData = zipFolder.zip();
			outputStream.write(zippedData);
			outputStream.flush();
		}
		catch (final IOException e)
		{
			LOGGER.error(e.getMessage());
			throw new ServiceException(e);
		}
	}

	@Override
	public void prepare() throws CoreException
	{
		/* empty method */
	}

	@Override
	protected void initService() throws CoreException
	{
		/* empty method */
	}

	@Override
	protected void closeService()
	{
		/* empty method */
	}
}
