// TEXT-TO-SPEECH FUNCTIONS FOR MINI-XPERT
let speechSynthesis = window.speechSynthesis;
let currentUtterance = null;

function stopSpeech() {
    if (speechSynthesis.speaking) {
        speechSynthesis.cancel();
        document.getElementById('speechStatus').style.display = 'none';
        document.getElementById('stopSpeechBtn').style.display = 'none';
        showToast("Speech stopped", "info");
    }
}

async function readHistory() {
    stopSpeech();

    try {
        const patient_id = localStorage.getItem("patient_id");
        if (!patient_id) {
            speak("Please log in to access your history.");
            return;
        }

        const res = await fetch(`${API}/patient/history?id_patient=${patient_id}`);

        if (!res.ok) {
            throw new Error("Failed to load history");
        }

        const history = await res.json();

        if (!history || history.length === 0) {
            speak("You have no medical history yet.");
            return;
        }

        let text = "Your medical history. ";

        history.forEach((item, index) => {
            const date = new Date(item.diagnosis_date).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });

            text += `Entry ${index + 1}. Date: ${date}. `;

            if (item.disease) {
                text += `Disease: ${item.disease}. `;
            }

            if (item.doctor_notes) {
                text += `Notes: ${item.doctor_notes}. `;
            }

            text += ". ";
        });

        speak(text);

    } catch (error) {
        console.error("Error in readHistory:", error);
        speak("Error loading medical history.");
        showToast("Error loading history", "error");
    }
}

async function readAppointments() {
    stopSpeech();

    try {
        const patient_id = localStorage.getItem("patient_id");
        if (!patient_id) {
            speak("Please log in to access your appointments.");
            return;
        }

        const res = await fetch(`${API}/patient/appointments?id_patient=${patient_id}`);

        if (!res.ok) {
            throw new Error("Failed to load appointments");
        }

        const appointments = await res.json();

        if (!appointments || appointments.length === 0) {
            speak("You have no upcoming appointments.");
            return;
        }

        let text = "Your appointments. ";

        appointments.forEach((apt, index) => {
            const date = new Date(apt.date);
            const dateStr = date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
            const timeStr = date.toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit'
            });

            text += `Appointment ${index + 1}. Date: ${dateStr} at ${timeStr}. `;

            if (apt.status) {
                text += `Status: ${apt.status}. `;
            }

            text += ". ";
        });

        speak(text);

    } catch (error) {
        console.error("Error in readAppointments:", error);
        speak("Error loading appointments.");
        showToast("Error loading appointments", "error");
    }
}

function speak(text) {
    console.log("Speaking:", text);

    if (!('speechSynthesis' in window)) {
        showToast("Speech not supported in this browser", "error");
        return;
    }

    if (speechSynthesis.speaking) {
        speechSynthesis.cancel();
    }

    // Wait a bit for cancel to complete
    setTimeout(() => {
        currentUtterance = new SpeechSynthesisUtterance(text);
        currentUtterance.lang = 'en-US';
        currentUtterance.rate = 0.9;
        currentUtterance.pitch = 1;
        currentUtterance.volume = 1;

        // Show status
        document.getElementById('speechStatus').style.display = 'block';
        document.getElementById('stopSpeechBtn').style.display = 'inline-block';
        document.getElementById('speechStatusText').textContent = 'Reading...';

        currentUtterance.onstart = () => {
            console.log("Speech started");
        };

        currentUtterance.onend = () => {
            console.log("Speech ended");
            document.getElementById('speechStatus').style.display = 'none';
            document.getElementById('stopSpeechBtn').style.display = 'none';
            showToast("Finished reading", "success");
        };

        currentUtterance.onerror = (event) => {
            console.error('Speech error:', event);
            document.getElementById('speechStatus').style.display = 'none';
            document.getElementById('stopSpeechBtn').style.display = 'none';
            showToast("Speech error: " + event.error, "error");
        };

        speechSynthesis.speak(currentUtterance);
    }, 100);
}

