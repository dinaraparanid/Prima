package com.dinaraparanid.prima.utils.web.happi

import com.google.gson.annotations.Expose

/**
 * Helps to parse response json string to fetch lyrics
 */

@Deprecated("Switched to Genius API")
class LyricsParser(
    @Expose val success: Boolean,
    @Expose val length: Int,
    @Expose val result: Lyrics
)