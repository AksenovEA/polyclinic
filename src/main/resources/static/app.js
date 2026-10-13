
const pageTitles = {
    home: "Главная",
    doctors: "Врачи и расписание",
    patients: "Пациенты",
    appointments: "Запись на приём",
    districts: "Участки"
};

const pageDescriptions = {
    home: "Информационная система поликлиники",
    patients: "Здесь появится список пациентов",
    appointments: "Здесь появится форма записи на приём"
};

const pageTitle = document.getElementById("page-title");
const pageContent = document.getElementById("page-content");


document.querySelectorAll(".nav-button").forEach(button => {
    button.addEventListener("click", () => {
        const page = button.dataset.page;

        document.querySelectorAll(".nav-button").forEach(item => {
            item.classList.remove("active");
        });

        button.classList.add("active");
        pageTitle.textContent = pageTitles[page];

        if (page === "doctors") {
            loadDoctors();
        } else if (page === "patients") {
            loadPatients();
        } else if (page === "appointments") {
            showAppointmentForm();
        } else if (page === "districts") {
            loadDistricts();
        }else {
            showHome();
        }
    });
});

function showHome() {
    pageContent.replaceChildren();

    const card = document.createElement("div");
    card.className = "welcome-card";

    const heading = document.createElement("h2");
    heading.textContent = "Добро пожаловать!";

    const description = document.createElement("p");
    description.textContent =
        "Здесь можно просматривать расписание врачей, " +
        "управлять пациентами и записывать на приём.";

    card.append(heading, description);
    pageContent.append(card);
}

function showPlaceholder(page) {
    pageContent.replaceChildren();

    const card = document.createElement("div");
    card.className = "welcome-card";

    const heading = document.createElement("h2");
    heading.textContent = pageTitles[page];

    const description = document.createElement("p");
    description.textContent =
        pageDescriptions[page] || "Раздел находится в разработке.";

    card.append(heading, description);
    pageContent.append(card);
}



async function loadDoctors() {
    pageContent.replaceChildren();

    const loading = document.createElement("p");
    loading.textContent = "Загружаем список врачей...";
    pageContent.append(loading);

    try {
        const response = await fetch("/api/doctors");

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const doctors = await response.json();

        pageContent.replaceChildren();

        const card = document.createElement("div");
        card.className = "welcome-card";

        const heading = document.createElement("h2");
        heading.textContent = "Список врачей";

        const addButton = document.createElement("button");
        addButton.className = "action-button";
        addButton.textContent = "+ Добавить врача";
        addButton.addEventListener("click", showDoctorForm);

        card.append(heading, addButton);

        const table = document.createElement("table");
        table.className = "data-table";

        const thead = document.createElement("thead");
        const headerRow = document.createElement("tr");

        ["ФИО", "Тип врача", "Участок", "Действия"].forEach(title => {
            const th = document.createElement("th");
            th.textContent = title;
            headerRow.append(th);
        });

        thead.append(headerRow);

        const tbody = document.createElement("tbody");

        if (doctors.length === 0) {
            const message = document.createElement("p");
            message.textContent = "Врачи пока не добавлены.";
            card.append(heading, message);
        } else {
            doctors.forEach(doctor => {
                const row = document.createElement("tr");

                const nameCell = document.createElement("td");
                nameCell.textContent = doctor.fullName;

                const typeCell = document.createElement("td");
                typeCell.textContent =
                    doctor.type === "THERAPIST"
                        ? "Терапевт"
                        : "Специалист";

                const districtCell = document.createElement("td");
                districtCell.textContent = doctor.district
                    ? `Участок № ${doctor.district.number}`
                    : "Не прикреплён";

                const actionCell = document.createElement("td");

                const scheduleButton = document.createElement("button");
                scheduleButton.className = "action-button";
                scheduleButton.textContent = "Посмотреть расписание";

                scheduleButton.addEventListener("click", () => {
                    showDoctorSchedule(doctor);
                });

                actionCell.append(scheduleButton);
                row.append(
                    nameCell,
                    typeCell,
                    districtCell,
                    actionCell
                );

                tbody.append(row);
            });

            table.append(thead, tbody);
            card.append(table);
        }

        pageContent.append(card);

    } catch (error) {
        showError(`Не удалось загрузить врачей. ${error.message}`);
    }
}


async function showDoctorForm() {
    let districts;

    try {
        const response = await fetch("/api/districts");

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        districts = await response.json();
    } catch (error) {
        showError(`Не удалось загрузить участки. ${error.message}`);
        return;
    }

    pageContent.replaceChildren();

    const card = document.createElement("div");
    card.className = "welcome-card";

    const heading = document.createElement("h2");
    heading.textContent = "Добавление врача";

    const form = document.createElement("form");
    form.className = "patient-form";

    // ФИО врача
    const nameWrapper = document.createElement("div");
    nameWrapper.className = "form-field";

    const nameLabel = document.createElement("label");
    nameLabel.htmlFor = "doctorFullName";
    nameLabel.textContent = "ФИО врача";

    const nameInput = document.createElement("input");
    nameInput.id = "doctorFullName";
    nameInput.name = "fullName";
    nameInput.type = "text";
    nameInput.maxLength = 255;
    nameInput.placeholder = "Иванов Иван Иванович";
    nameInput.required = true;

    nameWrapper.append(nameLabel, nameInput);

    // Тип врача
    const typeWrapper = document.createElement("div");
    typeWrapper.className = "form-field";

    const typeLabel = document.createElement("label");
    typeLabel.htmlFor = "doctorType";
    typeLabel.textContent = "Тип врача";

    const typeSelect = document.createElement("select");
    typeSelect.id = "doctorType";
    typeSelect.name = "type";
    typeSelect.required = true;

    typeSelect.add(new Option("Выберите тип врача", ""));
    typeSelect.add(new Option("Терапевт", "THERAPIST"));
    typeSelect.add(new Option("Специалист", "SPECIALIST"));

    typeWrapper.append(typeLabel, typeSelect);

    // Участок — обязателен только для терапевта
    const districtWrapper = document.createElement("div");
    districtWrapper.className = "form-field";

    const districtLabel = document.createElement("label");
    districtLabel.htmlFor = "doctorDistrict";
    districtLabel.textContent = "Участок";

    const districtSelect = document.createElement("select");
    districtSelect.id = "doctorDistrict";
    districtSelect.name = "districtId";

    districtSelect.add(new Option("Выберите участок", ""));

    districts.forEach(district => {
        districtSelect.add(new Option(
            `Участок № ${district.number}`,
            district.id
        ));
    });

    districtWrapper.append(districtLabel, districtSelect);
    districtWrapper.hidden = true;

    // Показываем участок только для терапевта
    typeSelect.addEventListener("change", () => {
        const isTherapist = typeSelect.value === "THERAPIST";

        districtWrapper.hidden = !isTherapist;
        districtSelect.required = isTherapist;

        if (!isTherapist) {
            districtSelect.value = "";
        }
    });

    const message = document.createElement("p");
    message.className = "form-message";

    const saveButton = document.createElement("button");
    saveButton.type = "submit";
    saveButton.className = "action-button";
    saveButton.textContent = "Сохранить врача";

    const backButton = document.createElement("button");
    backButton.type = "button";
    backButton.className = "action-button secondary-button";
    backButton.textContent = "Назад к списку";
    backButton.addEventListener("click", loadDoctors);

    form.append(
        nameWrapper,
        typeWrapper,
        districtWrapper,
        saveButton,
        backButton,
        message
    );

    card.append(heading, form);
    pageContent.append(card);

    form.addEventListener("submit", async event => {
        event.preventDefault();

        const fullName = nameInput.value.trim();
        const type = typeSelect.value;

        if (!fullName || !type) {
            message.textContent = "Заполни ФИО и выбери тип врача.";
            message.className = "form-message error-message";
            return;
        }

        if (
            type === "THERAPIST" &&
            !districtSelect.value
        ) {
            message.textContent = "Для терапевта необходимо выбрать участок.";
            message.className = "form-message error-message";
            return;
        }

        // Формируем объект в формате, который ожидает POST /api/doctors
        const doctor = {
            fullName,
            type
        };

        if (type === "THERAPIST") {
            doctor.district = {
                id: Number(districtSelect.value)
            };
        }

        saveButton.disabled = true;
        message.textContent = "Сохраняем врача...";
        message.className = "form-message";

        try {
            const response = await fetch("/api/doctors", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(doctor)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(
                    errorText || `Ошибка сервера: ${response.status}`
                );
            }

            alert("Врач успешно добавлен!");
            await loadDoctors();

        } catch (error) {
            message.textContent =
                `Не удалось сохранить врача: ${error.message}`;
            message.className = "form-message error-message";
        } finally {
            saveButton.disabled = false;
        }
    });
}


async function showDoctorSchedule(doctor) {
    pageContent.replaceChildren();

    const card = document.createElement("div");
    card.className = "welcome-card";

    const heading = document.createElement("h2");
    heading.textContent = `Расписание: ${doctor.fullName}`;

    const backButton = document.createElement("button");
    backButton.className = "action-button";
    backButton.textContent = "← К списку врачей";
    backButton.addEventListener("click", loadDoctors);

    const filters = document.createElement("div");
    filters.className = "schedule-filters";

    const monthSelect = document.createElement("select");
    const months = [
        "Январь", "Февраль", "Март", "Апрель",
        "Май", "Июнь", "Июль", "Август",
        "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    ];

    months.forEach((month, index) => {
        const option = document.createElement("option");
        option.value = index + 1;
        option.textContent = month;
        monthSelect.append(option);
    });

    const yearInput = document.createElement("input");
    yearInput.type = "number";
    yearInput.min = "2000";
    yearInput.max = "2100";
    yearInput.value = new Date().getFullYear();
    yearInput.setAttribute("aria-label", "Год");

    const loadButton = document.createElement("button");
    loadButton.className = "action-button";
    loadButton.textContent = "Показать";

    const result = document.createElement("div");
    result.className = "schedule-result";

    filters.append(monthSelect, yearInput, loadButton);
    card.append(heading, backButton, filters, result);
    pageContent.append(card);

    monthSelect.value = String(new Date().getMonth() + 1);

    loadButton.addEventListener("click", () => {
        loadDoctorMonth(
            doctor.id,
            Number(yearInput.value),
            Number(monthSelect.value),
            result
        );
    });

    await loadDoctorMonth(
        doctor.id,
        Number(yearInput.value),
        Number(monthSelect.value),
        result
    );
}

async function loadDoctorMonth(doctorId, year, month, result) {
    result.replaceChildren();

    if (!Number.isInteger(year) || year < 2000 || year > 2100) {
        result.textContent = "Укажи корректный год от 2000 до 2100.";
        return;
    }

    const loading = document.createElement("p");
    loading.textContent = "Загружаем расписание...";
    result.append(loading);

    try {
        const response = await fetch(
            `/api/doctors/${doctorId}/schedule?year=${year}&month=${month}`
        );

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const data = await response.json();
        result.replaceChildren();

        if (!data.days || data.days.length === 0) {
            const message = document.createElement("p");
            message.textContent = "На этот месяц расписание не найдено.";
            result.append(message);
            return;
        }

        data.days.forEach(day => {
            const dayCard = document.createElement("div");
            dayCard.className = "day-card";

            const dayHeading = document.createElement("h3");
            dayHeading.textContent =
                `${day.date} · ${day.startTime.slice(0, 5)}–` +
                `${day.endTime.slice(0, 5)}`;

            const table = document.createElement("table");
            table.className = "data-table";

            const thead = document.createElement("thead");
            const headerRow = document.createElement("tr");

            ["Время", "Статус", "Пациент"].forEach(title => {
                const th = document.createElement("th");
                th.textContent = title;
                headerRow.append(th);
            });

            thead.append(headerRow);

            const tbody = document.createElement("tbody");

            day.slots.forEach(slot => {
                const row = document.createElement("tr");

                const timeCell = document.createElement("td");
                timeCell.textContent = slot.time.slice(0, 5);

                const statusCell = document.createElement("td");
                statusCell.textContent =
                    slot.available ? "Свободно" : "Занято";
                statusCell.className = slot.available
                    ? "slot-free"
                    : "slot-busy";

                const patientCell = document.createElement("td");
                patientCell.textContent = slot.patientName ?? "—";

                row.append(timeCell, statusCell, patientCell);
                tbody.append(row);
            });

            table.append(thead, tbody);
            dayCard.append(dayHeading, table);
            result.append(dayCard);
        });

    } catch (error) {
        const message = document.createElement("p");
        message.className = "error-message";
        message.textContent =
            `Не удалось загрузить расписание. ${error.message}`;
        result.replaceChildren(message);
    }
}



async function loadPatients() {
    pageContent.replaceChildren();

    const loading = document.createElement("p");
    loading.textContent = "Загружаем список пациентов...";
    pageContent.append(loading);

    try {
        const response = await fetch("/api/patients");

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const patients = await response.json();

        pageContent.replaceChildren();

        const card = document.createElement("div");
        card.className = "welcome-card";

        const heading = document.createElement("h2");
        heading.textContent = "Список пациентов";

        const addButton = document.createElement("button");
        addButton.className = "action-button";
        addButton.textContent = "+ Добавить пациента";
        addButton.addEventListener("click", showPatientForm);

        card.append(heading, addButton);

        if (patients.length === 0) {
            const message = document.createElement("p");
            message.textContent = "Пациентов пока нет.";
            card.append(message);
        } else {
            const table = document.createElement("table");
            table.className = "data-table";

            const thead = document.createElement("thead");
            const headerRow = document.createElement("tr");

            ["ФИО", "Дата рождения", "Адрес", "Участок"].forEach(title => {
                const th = document.createElement("th");
                th.textContent = title;
                headerRow.append(th);
            });

            thead.append(headerRow);

            const tbody = document.createElement("tbody");

            patients.forEach(patient => {
                const row = document.createElement("tr");

                const nameCell = document.createElement("td");
                nameCell.textContent = patient.fullName;

                const birthDateCell = document.createElement("td");
                birthDateCell.textContent = patient.birthDate;

                const addressCell = document.createElement("td");
                addressCell.textContent = patient.address;

                const districtCell = document.createElement("td");
                districtCell.textContent = patient.district
                    ? `№ ${patient.district.number}`
                    : "Не указан";

                row.append(
                    nameCell,
                    birthDateCell,
                    addressCell,
                    districtCell
                );

                tbody.append(row);
            });

            table.append(thead, tbody);
            card.append(table);
        }

        pageContent.append(card);

    } catch (error) {
        showError(`Не удалось загрузить пациентов. ${error.message}`);
    }
}

async function showPatientForm() {
    let districts;

    try {
        const response = await fetch("/api/districts");

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        districts = await response.json();
    } catch (error) {
        alert(`Не удалось загрузить участки: ${error.message}`);
        return;
    }

    pageContent.replaceChildren();

    const card = document.createElement("div");
    card.className = "welcome-card";

    const heading = document.createElement("h2");
    heading.textContent = "Добавление пациента";

    const form = document.createElement("form");
    form.className = "patient-form";

    function createField(labelText, name, type = "text") {
        const wrapper = document.createElement("div");
        wrapper.className = "form-field";

        const label = document.createElement("label");
        label.textContent = labelText;
        label.htmlFor = name;

        const input = document.createElement("input");
        input.id = name;
        input.name = name;
        input.type = type;
        input.required = true;

        wrapper.append(label, input);
        return { wrapper, input };
    }

    const nameField = createField("ФИО", "fullName");
    nameField.input.maxLength = 255;
    nameField.input.placeholder = "Иванов Иван Иванович";

    const birthDateField = createField("Дата рождения", "birthDate", "date");

    const addressField = createField("Адрес", "address");
    addressField.input.maxLength = 255;
    addressField.input.placeholder = "Улица, дом, квартира";

    const districtWrapper = document.createElement("div");
    districtWrapper.className = "form-field";

    const districtLabel = document.createElement("label");
    districtLabel.textContent = "Участок";
    districtLabel.htmlFor = "districtId";

    const districtSelect = document.createElement("select");
    districtSelect.id = "districtId";
    districtSelect.name = "districtId";
    districtSelect.required = true;

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Выберите участок";
    placeholder.disabled = true;
    placeholder.selected = true;
    districtSelect.append(placeholder);

    districts.forEach(district => {
        const option = document.createElement("option");
        option.value = district.id;
        option.textContent = `Участок № ${district.number}`;
        districtSelect.append(option);
    });

    districtWrapper.append(districtLabel, districtSelect);

    const message = document.createElement("p");
    message.className = "form-message";

    const saveButton = document.createElement("button");
    saveButton.type = "submit";
    saveButton.className = "action-button";
    saveButton.textContent = "Сохранить";

    const backButton = document.createElement("button");
    backButton.type = "button";
    backButton.className = "action-button secondary-button";
    backButton.textContent = "Назад к списку";
    backButton.addEventListener("click", loadPatients);

    form.append(
        nameField.wrapper,
        birthDateField.wrapper,
        addressField.wrapper,
        districtWrapper,
        saveButton,
        backButton,
        message
    );

    card.append(heading, form);
    pageContent.append(card);

    form.addEventListener("submit", async event => {
        event.preventDefault();

        saveButton.disabled = true;
        message.textContent = "Сохраняем пациента...";
        message.className = "form-message";

        const patient = {
            fullName: nameField.input.value.trim(),
            birthDate: birthDateField.input.value,
            address: addressField.input.value.trim(),
            district: {
                id: Number(districtSelect.value)
            }
        };

        try {
            const response = await fetch("/api/patients", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(patient)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(
                    errorText || `Ошибка сервера: ${response.status}`
                );
            }

            alert("Пациент успешно добавлен!");
            await loadPatients();

        } catch (error) {
            message.textContent =
                `Не удалось сохранить пациента: ${error.message}`;
            message.className = "form-message error-message";
        } finally {
            saveButton.disabled = false;
        }
    });
}



async function showAppointmentForm() {
    pageContent.innerHTML = `
        <div class="welcome-card">
            <h2>Запись на приём</h2>

            <form id="appointment-form" class="patient-form">
                <div class="form-field">
                    <label for="appointment-patient">Пациент</label>
                    <select id="appointment-patient" required>
                        <option value="">Загрузка пациентов...</option>
                    </select>
                </div>

                <div class="form-field">
                    <label for="appointment-doctor">Врач</label>
                    <select id="appointment-doctor" required>
                        <option value="">Загрузка врачей...</option>
                    </select>
                </div>

                <div class="form-field">
                    <label for="appointment-date">Дата приёма</label>
                    <input id="appointment-date" type="date" required>
                </div>

                <div class="form-field">
                    <label for="appointment-time">Свободное время</label>
                    <select id="appointment-time" required disabled>
                        <option value="">Сначала выберите врача и дату</option>
                    </select>
                </div>

                <p id="appointment-message" class="form-message"></p>

                <button type="submit" class="action-button">
                    Записать пациента
                </button>
            </form>
        </div>
    `;

    const patientSelect = document.getElementById("appointment-patient");
    const doctorSelect = document.getElementById("appointment-doctor");
    const dateInput = document.getElementById("appointment-date");
    const timeSelect = document.getElementById("appointment-time");
    const message = document.getElementById("appointment-message");
    const form = document.getElementById("appointment-form");


    const today = new Date();
    const localToday = [
        today.getFullYear(),
        String(today.getMonth() + 1).padStart(2, "0"),
        String(today.getDate()).padStart(2, "0")
    ].join("-");

    dateInput.min = localToday;

    let selectedScheduleId = null;

    function resetTimeSelect(text) {
        selectedScheduleId = null;
        timeSelect.innerHTML = "";
        timeSelect.add(new Option(text, ""));
        timeSelect.disabled = true;
    }

    try {
        const [patientsResponse, doctorsResponse] = await Promise.all([
            fetch("/api/patients"),
            fetch("/api/doctors")
        ]);

        if (!patientsResponse.ok || !doctorsResponse.ok) {
            throw new Error("Не удалось загрузить список пациентов или врачей.");
        }

        const patients = await patientsResponse.json();
        const doctors = await doctorsResponse.json();

        patientSelect.innerHTML = '<option value="">Выберите пациента</option>';
        doctorSelect.innerHTML = '<option value="">Выберите врача</option>';

        patients.forEach(patient => {
            patientSelect.add(new Option(
                `${patient.fullName} (ID: ${patient.id})`,
                patient.id
            ));
        });

        doctors.forEach(doctor => {
            doctorSelect.add(new Option(
                `${doctor.fullName} (ID: ${doctor.id})`,
                doctor.id
            ));
        });

        if (patients.length === 0) {
            message.textContent = "Сначала добавьте хотя бы одного пациента.";
        }

        if (doctors.length === 0) {
            message.textContent = "Сначала добавьте хотя бы одного врача.";
        }
    } catch (error) {
        message.textContent = error.message;
        return;
    }

    async function loadAvailableTimes() {
        resetTimeSelect("Загрузка свободного времени...");
        message.textContent = "";

        const doctorId = doctorSelect.value;
        const date = dateInput.value;

        if (!doctorId || !date) {
            resetTimeSelect("Выберите врача и дату");
            return;
        }

        const [year, month] = date.split("-").map(Number);

        try {
            const response = await fetch(
                `/api/doctors/${doctorId}/schedule?year=${year}&month=${month}`
            );

            if (!response.ok) {
                throw new Error("Не удалось загрузить расписание врача.");
            }

            const data = await response.json();


            const day = data.days.find(item => item.date === date);

            if (!day) {
                resetTimeSelect("На эту дату приёма нет");
                return;
            }

            selectedScheduleId = day.scheduleId;

            const freeSlots = day.slots.filter(slot => slot.available);

            if (freeSlots.length === 0) {
                resetTimeSelect("Свободных слотов нет");
                return;
            }

            timeSelect.innerHTML = '<option value="">Выберите время</option>';

            freeSlots.forEach(slot => {
                const time = slot.time.slice(0, 5);
                timeSelect.add(new Option(time, slot.time));
            });

            timeSelect.disabled = false;
        } catch (error) {
            resetTimeSelect("Не удалось загрузить время");
            message.textContent = error.message;
        }
    }

    doctorSelect.addEventListener("change", loadAvailableTimes);
    dateInput.addEventListener("change", loadAvailableTimes);

    form.addEventListener("submit", async event => {
        event.preventDefault();
        message.textContent = "";

        const patientId = patientSelect.value;
        const appointmentTime = timeSelect.value;

        if (!patientId || !doctorSelect.value || !dateInput.value ||
            !appointmentTime || !selectedScheduleId) {
            message.textContent = "Выберите пациента, врача, дату и свободное время.";
            return;
        }

        try {
            const response = await fetch("/api/appointments", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    schedule: { id: selectedScheduleId },
                    patient: { id: Number(patientId) },
                    appointmentTime: appointmentTime
                })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => null);
                throw new Error(
                    errorData?.detail ||
                    errorData?.message ||
                    "Не удалось создать запись. Возможно, слот уже занят."
                );
            }

            message.textContent = "Пациент успешно записан на приём!";
            await loadAvailableTimes();
        } catch (error) {
            message.textContent = error.message;
        }
    });
}


async function loadDistricts() {
    pageContent.replaceChildren();

    try {
        const response = await fetch("/api/districts");

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const districts = await response.json();

        const card = document.createElement("div");
        card.className = "welcome-card";

        const heading = document.createElement("h2");
        heading.textContent = "Список участков";

        const addButton = document.createElement("button");
        addButton.className = "action-button";
        addButton.textContent = "+ Добавить участок";
        addButton.addEventListener("click", showDistrictForm);

        card.append(heading, addButton);

        if (districts.length === 0) {
            const message = document.createElement("p");
            message.textContent = "Участки пока не добавлены.";
            card.append(message);
        } else {
            const table = document.createElement("table");
            table.className = "data-table";

            const thead = document.createElement("thead");
            const headerRow = document.createElement("tr");

            ["Номер участка", "Адрес"].forEach(title => {
                const th = document.createElement("th");
                th.textContent = title;
                headerRow.append(th);
            });

            thead.append(headerRow);

            const tbody = document.createElement("tbody");

            districts.forEach(district => {
                const row = document.createElement("tr");

                const numberCell = document.createElement("td");
                numberCell.textContent = district.number;

                const addressCell = document.createElement("td");
                addressCell.textContent = district.address;

                row.append(numberCell, addressCell);
                tbody.append(row);
            });

            table.append(thead, tbody);
            card.append(table);
        }

        pageContent.replaceChildren(card);
    } catch (error) {
        showError(`Не удалось загрузить участки: ${error.message}`);
    }
}


async function showDistrictForm() {
    pageContent.replaceChildren();

    const card = document.createElement("div");
    card.className = "welcome-card";

    const heading = document.createElement("h2");
    heading.textContent = "Добавление участка";

    const form = document.createElement("form");
    form.className = "patient-form";

    function createField(labelText, name, type = "text") {
        const wrapper = document.createElement("div");
        wrapper.className = "form-field";

        const label = document.createElement("label");
        label.htmlFor = name;
        label.textContent = labelText;

        const input = document.createElement("input");
        input.id = name;
        input.name = name;
        input.type = type;
        input.required = true;

        wrapper.append(label, input);

        return { wrapper, input };
    }

    const numberField = createField(
        "Номер участка",
        "districtNumber",
        "number"
    );

    numberField.input.min = "1";
    numberField.input.step = "1";
    numberField.input.placeholder = "Например, 12";

    const addressField = createField(
        "Адрес участка",
        "districtAddress"
    );

    addressField.input.maxLength = 255;
    addressField.input.placeholder = "Улица, дом";

    const message = document.createElement("p");
    message.className = "form-message";

    const saveButton = document.createElement("button");
    saveButton.type = "submit";
    saveButton.className = "action-button";
    saveButton.textContent = "Сохранить участок";

    const backButton = document.createElement("button");
    backButton.type = "button";
    backButton.className = "action-button secondary-button";
    backButton.textContent = "Назад к списку";
    backButton.addEventListener("click", loadDistricts);

    form.append(
        numberField.wrapper,
        addressField.wrapper,
        saveButton,
        backButton,
        message
    );

    card.append(heading, form);
    pageContent.append(card);

    form.addEventListener("submit", async event => {
        event.preventDefault();

        const number = Number(numberField.input.value);
        const address = addressField.input.value.trim();

        if (!Number.isInteger(number) || number <= 0) {
            message.textContent =
                "Номер участка должен быть положительным целым числом.";
            message.className = "form-message error-message";
            return;
        }

        if (!address) {
            message.textContent = "Укажи адрес участка.";
            message.className = "form-message error-message";
            return;
        }

        const district = { number, address };

        saveButton.disabled = true;
        message.textContent = "Сохраняем участок...";
        message.className = "form-message";

        try {
            const response = await fetch("/api/districts", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(district)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(
                    errorText || `Ошибка сервера: ${response.status}`
                );
            }

            alert("Участок успешно добавлен!");
            await loadDistricts();
        } catch (error) {
            message.textContent =
                `Не удалось сохранить участок: ${error.message}`;
            message.className = "form-message error-message";
        } finally {
            saveButton.disabled = false;
        }
    });
}

function showError(text) {
    pageContent.replaceChildren();

    const message = document.createElement("p");
    message.className = "error-message";
    message.textContent = text;

    pageContent.append(message);
}
