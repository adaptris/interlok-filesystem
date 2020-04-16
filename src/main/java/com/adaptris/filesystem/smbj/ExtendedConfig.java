package com.adaptris.filesystem.smbj;

import java.util.Optional;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.TimeInterval;
import com.hierynomus.smbj.SmbConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Exposes additional seetings for SMB configuration.
 * 
 * <p>
 * Generally if they are set, then the underlying {@code SmbConfig#builder()} will have those things set against it. No validation
 * is done on the values and you should consult the Microsoft documentation to see what they mean.
 * </p>
 * 
 * @config smb-extended-config
 */
@XStreamAlias("smb-extended-config")
@DisplayOrder(
    order = {"workstationName", "signingRequired", "dfsEnabled", "multiProtocolNegotiate", "bufferSize", "timeout",
        "transactTimeout", "soTimeout"})
@NoArgsConstructor
@ComponentProfile(summary = "Extended SMB configuration")
public class ExtendedConfig implements ConfigProvider {

  @Getter
  @Setter
  private String workstationName;
  @Getter
  @Setter
  private Boolean signingRequired;
  @Getter
  @Setter
  private Boolean dfsEnabled;
  @Getter
  @Setter
  private Boolean multiProtocolNegotiate;
  /**
   * Effectively sets the Read/Write/Transact Buffersize.
   * 
   */
  @Getter
  @Setter
  private Integer bufferSize;
  @Getter
  @Setter
  private TimeInterval soTimeout;
  /**
   * Effectively sets the Read/Write/Transact timeout.
   * 
   */
  @Getter
  @Setter
  private TimeInterval timeout;
  
  @Override
  public SmbConfig smbConfig() throws Exception {
    SmbConfig.Builder builder = SmbConfig.builder();
    Optional.ofNullable(getSigningRequired()).ifPresent((b) -> builder.withSigningRequired(b));
    Optional.ofNullable(getDfsEnabled()).ifPresent((b) -> builder.withDfsEnabled(b));
    Optional.ofNullable(getMultiProtocolNegotiate()).ifPresent((b) -> builder.withMultiProtocolNegotiate(b));
    Optional.ofNullable(getBufferSize()).ifPresent((b) -> builder.withBufferSize(b));
    Optional.ofNullable(getSoTimeout()).ifPresent((b) -> builder.withSoTimeout(b.getInterval(), b.getUnit()));
    Optional.ofNullable(getTimeout()).ifPresent((b) -> builder.withTimeout(b.getInterval(), b.getUnit()));
    Optional.ofNullable(getWorkstationName()).ifPresent((b) -> builder.withWorkStationName(getWorkstationName()));
    return builder.build();
  }

}
