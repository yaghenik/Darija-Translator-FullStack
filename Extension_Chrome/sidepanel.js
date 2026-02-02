document.addEventListener('DOMContentLoaded', function() {
    const translateBtn = document.getElementById('translateBtn');
    const inputText = document.getElementById('inputText');
    const resultContainer = document.getElementById('result-container');
    const resultText = document.getElementById('result-text');
    const errorMessage = document.getElementById('errorMessage');
    const autofillToggle = document.getElementById('autofillToggle');

    // Variable pour stocker le "chronomètre" de la boucle
    let autofillInterval = null;

    // --- 1. LOGIQUE AUTOFILL DYNAMIQUE ---
    autofillToggle.addEventListener('change', function() {
        if (this.checked) {
            console.log("Autofill activé : Démarrage de la surveillance...");
            
            // 1. On vérifie tout de suite
            getSelectionFromActiveTab();

            // 2. On lance une boucle qui vérifie toutes les 1 seconde (1000ms)
            autofillInterval = setInterval(getSelectionFromActiveTab, 1000);
        
        } else {
            console.log("Autofill désactivé : Arrêt de la surveillance.");
            
            // On arrête la boucle
            if (autofillInterval) {
                clearInterval(autofillInterval);
                autofillInterval = null;
            }
        }
    });

    async function getSelectionFromActiveTab() {
        try {
            // Cherche l'onglet actif
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            
            // Vérification de sécurité
            if (!tab || !tab.url || tab.url.startsWith("chrome://") || tab.url.startsWith("edge://")) {
                return; // On ne fait rien sur les pages système
            }

            // Injection du script pour lire la sélection
            const result = await chrome.scripting.executeScript({
                target: { tabId: tab.id },
                func: () => window.getSelection().toString()
            });

            if (result && result[0] && result[0].result) {
                const selectedText = result[0].result.trim();
                
                // --- MODIFICATION IMPORTANTE ---
                // On met à jour SEULEMENT si le texte est différent de ce qu'on a déjà
                // et si le texte n'est pas vide.
                if (selectedText && selectedText !== inputText.value) {
                    console.log("Nouveau texte détecté :", selectedText);
                    inputText.value = selectedText;
                    
                    // Petit effet visuel
                    inputText.style.borderColor = "#A78BFA";
                    setTimeout(() => inputText.style.borderColor = "", 500);
                }
            }
        } catch (err) {
            // On ignore les erreurs silencieuses (ex: changement d'onglet rapide)
        }
    }

    // --- 2. LOGIQUE TRADUCTION ---
    translateBtn.addEventListener('click', function() {
        const text = inputText.value.trim();

        // Reset
        errorMessage.style.display = 'none';
        resultText.textContent = "Translating...";
        resultContainer.classList.remove('success');

        if (!text) {
            showError("Please enter some text.");
            resultText.textContent = "Translation will appear here...";
            return;
        }

        setLoadingState(true);

        const apiUrl = "http://localhost:8080/darija-translator/api/translate?word=" + encodeURIComponent(text);
        const authHeader = "Basic " + btoa("admin:admin");

        fetch(apiUrl, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Authorization': authHeader
            }
        })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) return Promise.reject("Auth failed.");
                return Promise.reject("Server Error: " + response.status);
            }
            return response.json();
        })
        .then(data => {
            setLoadingState(false);
            if (data.success === "true") {
                resultText.textContent = data.translation;
                resultContainer.classList.add('success');
            } else {
                showError(data.error || "Unknown error");
                resultText.textContent = "Translation will appear here...";
            }
        })
        .catch(error => {
            setLoadingState(false);
            console.error(error);
            showError("Connection failed.");
            resultText.textContent = "Translation will appear here...";
        });
    });

    function setLoadingState(isLoading) {
        if (isLoading) {
            translateBtn.classList.add('loading');
            translateBtn.disabled = true;
        } else {
            translateBtn.classList.remove('loading');
            translateBtn.disabled = false;
        }
    }

    function showError(msg) {
        errorMessage.textContent = msg;
        errorMessage.style.display = 'block';
    }
});