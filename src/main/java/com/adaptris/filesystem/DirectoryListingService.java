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
import java.util.List;
import java.util.Locale;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.enhanced.FileSorter;
import com.adaptris.core.fs.enhanced.NoSorting;
import com.adaptris.core.services.metadata.DateFormatBuilder;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
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

	@Valid
	@AutoPopulated
	private DirectoryListingProvider directoryListingProvider;

	@NotNull
	@Valid
	private DataInputParameter<String> directory;

	@NotNull
	@Valid
	private DataOutputParameter<String> output;

	@NotNull
	@Valid
	@AutoPopulated
	@AdvancedConfig
	private FileSorter fileSorter;

	@Valid
	@AutoPopulated
	@AdvancedConfig
	private DateFormatBuilder dateFormatBuilder;

	public DirectoryListingService(){
		setFileSorter(new NoSorting());
		setDirectoryListingProvider(new DirectoryListingProviderDefault());
		setDateFormatBuilder(new DateFormatBuilder(DateFormatBuilder.DEFAULT_DATE_FORMAT));
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(final AdaptrisMessage message) throws ServiceException
	{
		NumberFormat.getNumberInstance(Locale.UK);
		try
		{
			final String path = getDirectory().extract(message);
			final File directory = new File(path);
			log.trace("ls: {} ", directory.getAbsolutePath());
			DateFormatBuilder.DateFormatter dateFormatter = getDateFormatBuilder().build(message);
			if (directory.exists() && directory.isDirectory())
			{
				final JSONArray entities = new JSONArray();
				final List<File> files = getFileSorter().sort(getDirectoryListingProvider().getFiles(directory));
				for (final File file : files)
				{
					log.trace("Found file: {}", file.getName());
					
					DirectoryEntity entity = new DirectoryEntity(file);
					entities.put(entity.toJSONObject(dateFormatter));
				}
				getOutput().insert(entities.toString(), message);
			}
			else
			{
				log.trace("Directory does not exist: {}", path);
				getOutput().insert("[]", message);
			}
		}
		catch (final JSONException | InterlokException e)
		{
			log.error(e.getMessage());
			throw new ServiceException(e);
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
	}

	public DataInputParameter<String> getDirectory() {
		return directory;
	}

	public void setDirectory(DataInputParameter<String> directory) {
		this.directory = Args.notNull(directory, "directory");
	}

	public DataOutputParameter<String> getOutput() {
		return output;
	}

	public void setOutput(DataOutputParameter<String> output) {
		this.output = Args.notNull(output, "output");
	}

	public DirectoryListingProvider getDirectoryListingProvider() {
		return directoryListingProvider;
	}

	public void setDirectoryListingProvider(DirectoryListingProvider directoryListingProvider) {
		this.directoryListingProvider = directoryListingProvider;
	}

	public FileSorter getFileSorter() {
		return fileSorter;
	}

	public void setFileSorter(FileSorter fileSorter) {
		this.fileSorter = fileSorter;
	}

	public DateFormatBuilder getDateFormatBuilder() {
		return dateFormatBuilder;
	}

	public void setDateFormatBuilder(DateFormatBuilder dateFormatBuilder) {
		this.dateFormatBuilder = dateFormatBuilder;
	}
}


