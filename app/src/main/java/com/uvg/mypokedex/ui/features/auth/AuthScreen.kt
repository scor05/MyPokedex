package com.uvg.mypokedex.ui.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    // Si ya está autenticado, ir directamente a Home
    LaunchedEffect(uiState) {
        if (uiState is AuthUIState.Authenticated) {
            onAuthSuccess()
        }
    }

    var authMode by remember { mutableStateOf(AuthMode.ALIAS) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo o título
                    Text(
                        text = "🎮 MyPokedex",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = when (authMode) {
                            AuthMode.ALIAS -> "Inicio Rápido"
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

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Contenido según modo
                    when (authMode) {
                        AuthMode.ALIAS -> AliasAuthContent(
                            formState = formState,
                            onAliasChange = viewModel::updateAlias,
                            onSubmit = {
                                viewModel.signInWithAlias(formState.alias)
                            }
                        )
                        AuthMode.EMAIL_LOGIN -> EmailLoginContent(
                            formState = formState,
                            onEmailChange = viewModel::updateEmail,
                            onPasswordChange = viewModel::updatePassword,
                            onSubmit = {
                                viewModel.signInWithEmail(formState.email, formState.password)
                            }
                        )
                        AuthMode.EMAIL_SIGNUP -> EmailSignupContent(
                            formState = formState,
                            onEmailChange = viewModel::updateEmail,
                            onPasswordChange = viewModel::updatePassword,
                            onDisplayNameChange = viewModel::updateDisplayName,
                            onSubmit = {
                                viewModel.signUpWithEmail(
                                    formState.email,
                                    formState.password,
                                    formState.displayName
                                )
                            }
                        )
                    }

                    // Cambiar modo de autenticación
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (authMode != AuthMode.ALIAS) {
                                OutlinedButton(
                                    onClick = { authMode = AuthMode.ALIAS },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rápido", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            if (authMode != AuthMode.EMAIL_LOGIN) {
                                OutlinedButton(
                                    onClick = { authMode = AuthMode.EMAIL_LOGIN },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Email", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            if (authMode == AuthMode.EMAIL_LOGIN) {
                                OutlinedButton(
                                    onClick = { authMode = AuthMode.EMAIL_SIGNUP },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Registrar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Loading overlay
            if (formState.isLoading) {
                CircularProgressIndicator()
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
            text = "Ingresa un alias para competir (3-20 caracteres)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = formState.alias,
            onValueChange = onAliasChange,
            label = { Text("Alias (ej: POKE123)") },
            placeholder = { Text("Mi alias único") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true,
            supportingText = {
                Text("${formState.alias.length}/20 caracteres")
            }
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading && formState.alias.length in 3..20
        ) {
            if (formState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Entrar Rápido")
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
            placeholder = { Text("correo@ejemplo.com") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = formState.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            placeholder = { Text("••••••") },
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
            placeholder = { Text("Tu nombre") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = formState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            placeholder = { Text("correo@ejemplo.com") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true
        )

        OutlinedTextField(
            value = formState.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña (mín. 6 caracteres)") },
            placeholder = { Text("••••••") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isLoading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                Text("Mínimo 6 caracteres")
            }
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