package com.mubarak.madexample.data.sources

import com.mubarak.madexample.data.sources.local.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun insertNote(note: Note)
    suspend fun upsertNote(note: Note)
     fun getAllNote(): Flow<List<Note>>
    suspend fun deleteNote(note: Note)
    suspend fun deleteNoteById(noteId:Int)
    fun searchNote(searchQuery:String):Flow<List<Note>>
    fun getNoteById(noteId:String):Flow<Note>

}