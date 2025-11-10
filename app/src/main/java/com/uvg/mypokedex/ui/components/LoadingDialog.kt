package com.uvg.mypokedex.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uvg.mypokedex.ui.features.auth.AuthFormState
import com.uvg.mypokedex.ui.features.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsState()
    var authMode by remember { mutableStateOf(AuthMode.ALIAS) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = when (authMode) {
                        AuthMode.ALIAS -> "Iniciar Sesión Rápida"
                        AuthMode.EMAIL_LOGIN -> "Iniciar Sesión"
                        AuthMode.EMAIL_SIGNUP -> "Crear Cuenta"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Mensaje de error
                if (formState.errorMessage != null) {
                    Text(
                        text = formState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Contenido según modo
                when (authMode) {
                    AuthMode.ALIAS -> AliasAuthContent(
                        formState = formState,
                        onAliasChange = viewModel::updateAlias,
                        onSubmit = {
                            scope.launch {
                                viewModel.signInWithAlias(formState.alias)

                                kotlinx.coroutines.delay(500)
                                if (viewModel.isAuthenticated()) {
                                    onAuthSuccess()
                                }
                            }
                        }
                    )
                    AuthMode.EMAIL_LOGIN -> EmailLoginContent(
                        formState = formState,
                        onEmailChange = viewModel::updateEmail,
                        onPasswordChange = viewModel::updatePassword,
                        onSubmit = {
                            scope.launch {
                                viewModel.signInWithEmail(formState.email, formState.password)
                                kotlinx.coroutines.delay(500)
                                if (viewModel.isAuthenticated()) {
                                    onAuthSuccess()
                                }
                            }
                        }
                    )
                    AuthMode.EMAIL_SIGNUP -> EmailSignupContent(
                        formState = formState,
                        onEmailChange = viewModel::updateEmail,
                        onPasswordChange = viewModel::updatePassword,
                        onDisplayNameChange = viewModel::updateDisplayName,
                        onSubmit = {
                            scope.launch {
                                viewModel.signUpWithEmail(
                                    formState.email,
                                    formState.password,
                                    formState.displayName
                                )
                                kotlinx.coroutines.delay(500)
                                if (viewModel.isAuthenticated()) {
                                    onAuthSuccess()
                                }
                            }
                        }
                    )
                }

                // Cambiar modo de autenticación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (authMode != AuthMode.ALIAS) {
                        TextButton(
                            onClick = { authMode = AuthMode.ALIAS },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rápida")
                        }
                    }
                    if (authMode != AuthMode.EMAIL_LOGIN) {
                        TextButton(
                            onClick = { authMode = AuthMode.EMAIL_LOGIN },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Email")
                        }
                    }
                    if (authMode != AuthMode.EMAIL_SIGNUP && authMode == AuthMode.EMAIL_LOGIN) {
                        TextButton(
                            onClick = { authMode = AuthMode.EMAIL_SIGNUP },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Crear")
                        }
                    }
                }

                // Botón cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun AliasAuthContent(
    formState: AuthFormState,
    onAliasChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Ingresa un alias (3-20 caracteres)",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = formState.alias,
            onValueChange = onAliasChange,
            label = { Text("Alias (ej: POKE123)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading && formState.alias.length >= 3
        ) {
            if (formState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Entrar")
            }
        }
    }
}

@Composable
private fun EmailLoginContent(
    formState: AuthFormState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = formState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = formState.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading &&
                    formState.email.isNotBlank() &&
                    formState.password.isNotBlank()
        ) {
            if (formState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar Sesión")
            }
        }
    }
}

@Composable
private fun EmailSignupContent(
    formState: AuthFormState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = formState.displayName,
            onValueChange = onDisplayNameChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = formState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = formState.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña (mín. 6 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading &&
                    formState.email.isNotBlank() &&
                    formState.password.length >= 6 &&
                    formState.displayName.isNotBlank()
        ) {
            if (formState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Crear Cuenta")
            }
        }
    }
}

private enum class AuthMode {
    ALIAS,
    EMAIL_LOGIN,
    EMAIL_SIGNUP
}