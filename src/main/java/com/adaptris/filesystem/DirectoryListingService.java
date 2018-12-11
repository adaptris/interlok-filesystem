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

import com.adaptris.annotation.*;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.fs.enhanced.FileSorter;
import com.adaptris.core.fs.enhanced.NoSorting;
import com.adaptris.core.services.metadata.DateFormatBuilder;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.json.JSONArray;
import org.json.JSONException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
	 * The metadata key to export file listing data to.
	 */
	@Deprecated
	private String metadataKey;

	/**
	 * The folder to get a directory listing of.
	 */
	@InputFieldHint(expression = true)
	@Deprecated
	private String directoryPath;

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
		if (getDirectoryPath() != null)
		{
			log.warn("directoryPath is deprecated use directory instead.");
			if(getDirectory() == null) {
				setDirectory(new ConstantDataInputParameter(getDirectoryPath()));
			} else {
				log.warn("directory and directoryPath set using directory");
			}
		}
		if(getOutput() == null){
			if(getMetadataKey() != null) {
				log.warn("relying on metadataKey is deprecated use output instead.");
				setOutput(new MetadataDataOutputParameter(getMetadataKey()));
			} else {
				log.warn("relying on metadataKey is deprecated use output instead.");
				setOutput(new StringPayloadDataOutputParameter());
			}
		} else {
			if(getMetadataKey() != null) {
				log.warn("output and metadataKey set using output");
			}
		}

	}

	/**
	 * Get the directory path parameter.
	 *
	 * @return The directory path parameter.
	 */
	@Deprecated
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
	@Deprecated
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


