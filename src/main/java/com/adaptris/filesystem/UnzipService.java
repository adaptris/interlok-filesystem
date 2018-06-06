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
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Unzip a file and set the path to the root directory of the unzipped data in the payload.
 *
 * @config unzip-service
 */
@AdapterComponent
@ComponentProfile(summary = "Zip the contents of the message", tag = "service,zip,unzip")
@XStreamAlias("unzip-service")
public class UnzipService extends ServiceImp
{
	private static final Logger LOGGER = LoggerFactory.getLogger(UnzipService.class);

	@Override
	public void doService(final AdaptrisMessage msg) throws ServiceException
	{
		final File messageTempDirectory = new File(System.getProperty("java.io.tmpdir"), msg.getUniqueId());
		LOGGER.info("Extracting to temporary directory : " + messageTempDirectory);
		if (messageTempDirectory.exists())
		{
			throw new ServiceException(messageTempDirectory + " already exists.");
		}

		messageTempDirectory.mkdir();

		final ZipFolder zipFolder = new ZipFolder(messageTempDirectory.getAbsolutePath());
		try
		{
			final String unzippedDirPath = zipFolder.unzip(msg.getInputStream());
			LOGGER.info("Unzipped directory path : " + unzippedDirPath);
			msg.setContent(unzippedDirPath, msg.getContentEncoding());
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
