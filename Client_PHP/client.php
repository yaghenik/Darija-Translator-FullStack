<?php
// Configuration de l'accès à l'API Java
$api_url = "http://localhost:8080/darija-translator/api/translate";
$username = "admin"; // Le login défini dans AuthenticationFilter.java
$password = "admin"; // Le mot de passe défini dans AuthenticationFilter.java
$result_text = "";
$error_msg = "";

// Si le formulaire est soumis
if (isset($_POST['word']) && !empty($_POST['word'])) {
    $word = $_POST['word'];
    
    // 1. Préparer l'URL avec le paramètre
    $request_url = $api_url . "?word=" . urlencode($word);

    // 2. Initialiser cURL (Le client HTTP de PHP)
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $request_url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    
    // 3. IMPORTANT : Ajouter l'authentification Basic (La "Clé")
    curl_setopt($ch, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
    curl_setopt($ch, CURLOPT_USERPWD, "$username:$password");

    // 4. Exécuter la requête
    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    
    if(curl_errno($ch)){
        $error_msg = 'Erreur cURL : ' . curl_error($ch);
    } else {
        curl_close($ch);

        // 5. Traiter la réponse (JSON)
        if ($http_code == 200) {
            $data = json_decode($response, true);
            if (isset($data['translation'])) {
                $result_text = $data['translation'];
            } else {
                $error_msg = "Erreur : " . ($data['error'] ?? "Réponse invalide");
            }
        } elseif ($http_code == 401) {
            $error_msg = "Erreur 401 : Mot de passe incorrect (Vérifiez client.php)";
        } else {
            $error_msg = "Erreur serveur ($http_code)";
        }
    }
}
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Client PHP - Traducteur Darija</title>
    <style>
        body { font-family: sans-serif; background: #f0f2f5; display: flex; justify-content: center; padding-top: 50px; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 400px; text-align: center; }
        input[type="text"] { width: 80%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 4px; }
        button { background: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
        button:hover { background: #0056b3; }
        .result { margin-top: 20px; font-size: 1.5em; color: #28a745; font-weight: bold; }
        .error { color: red; margin-top: 20px; }
    </style>
</head>
<body>

<div class="container">
    <h2>Client PHP (Consommateur)</h2>
    <p>Ce client possède les clés pour parler à l'API sécurisée.</p>
    
    <form method="post">
        <input type="text" name="word" placeholder="Mot en anglais (ex: Friend)" required>
        <br>
        <button type="submit">Traduire en Darija</button>
    </form>

    <?php if ($result_text): ?>
        <div class="result">
            <?= htmlspecialchars($result_text) ?>
        </div>
    <?php endif; ?>

    <?php if ($error_msg): ?>
        <div class="error">
            <?= htmlspecialchars($error_msg) ?>
        </div>
    <?php endif; ?>
</div>

</body>
</html>