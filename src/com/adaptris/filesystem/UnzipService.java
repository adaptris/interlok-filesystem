package com.adaptris.filesystem;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

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
	private static final Logger LOGGER = Logger.getLogger(UnzipService.class);

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
