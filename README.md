# nodemcu-tv-sitter
Select viewable channels based on current time, duration and others.
Currently planned only for Philips TV.

This is basically meant to aid children in reasonable TV consumption.

Uses Philips TV's REST interface.

- restrict viewing of specific channels at certain times of day.
- Limit viewing time. E.g. allow TV usage for 1 Hour in the afternoon.
- Disable limitations based on presence of certain devices in the local network.
  If parents Cellphone is there no limitation is applied
  
This is desigend to work with the marcoskirsch/nodemcu-httpserver
