package com.example.vicente_parrra

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.vicente_parrra.model.Post
import com.example.vicente_parrra.ui.screen.ListaPosts
import com.example.vicente_parrra.viewmodel.PostEstado
import org.junit.Rule
import org.junit.Test

class PostScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun muestraPublicacion() {
        compose.setContent {
            MaterialTheme {
                ListaPosts(PostEstado(posts = listOf(Post(1, "Aviso de turno", "Nuevo horario"))),
                    {}, { _, _ -> }, {}, {})
            }
        }
        compose.onNodeWithText("Aviso de turno").assertExists()
        compose.onNodeWithText("Publicaciones").assertIsDisplayed()
    }
}
