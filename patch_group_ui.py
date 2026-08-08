import os

with open('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', 'r') as f:
    code = f.read()

# Add permission helper function
if 'fun canEditGroup' not in code:
    code = code.replace(
        "fun GroupProfileScreen(",
        """fun canEditGroup(role: String, field: String): Boolean {
    val r = role.lowercase()
    return when(field) {
        "banner", "name" -> r == "owner"
        "avatar", "description" -> r == "owner" || r == "admin"
        else -> false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupProfileScreen("""
    )

# Patch clickable for banner
code = code.replace(
    """.clickable { bannerLauncher.launch("image/*") }""",
    """.clickable { 
                                    if (canEditGroup(currentUserRole, "banner")) bannerLauncher.launch("image/*")
                                    else Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                }"""
)

# Patch clickable for avatar
code = code.replace(
    """.clickable { avatarLauncher.launch("image/*") }""",
    """.clickable { 
                                    if (canEditGroup(currentUserRole, "avatar")) avatarLauncher.launch("image/*")
                                    else Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                }"""
)

# Patch clickable for name
code = code.replace(
    """.clickable {
                        editNameText = groupName
                        showEditName = true
                    }""",
    """.clickable {
                        if (canEditGroup(currentUserRole, "name")) {
                            editNameText = groupName
                            showEditName = true
                        } else {
                            Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                        }
                    }"""
)

# Patch clickable for description
code = code.replace(
    """.clickable {
                                editDescriptionText = groupDesc
                                showEditDescription = true
                            }""",
    """.clickable {
                                if (canEditGroup(currentUserRole, "description")) {
                                    editDescriptionText = groupDesc
                                    showEditDescription = true
                                } else {
                                    Toast.makeText(context, "Anda bukan admin/owner", Toast.LENGTH_SHORT).show()
                                }
                            }"""
)


with open('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', 'w') as f:
    f.write(code)

print("Patched GroupProfileScreen UI with role checks")
