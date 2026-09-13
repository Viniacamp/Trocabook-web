// --- Função de Validação de CPF (Mantida como está) ---
function validarCPF(cpf) {
    cpf = cpf.replace(/[^\d]+/g, '');
    if (cpf === '') return false;
    // Elimina CPFs inválidos conhecidos
    if (cpf.length !== 11 ||
        cpf === "00000000000" || cpf === "11111111111" || cpf === "22222222222" ||
        cpf === "33333333333" || cpf === "44444444444" || cpf === "55555555555" ||
        cpf === "66666666666" || cpf === "77777777777" || cpf === "88888888888" ||
        cpf === "99999999999")
        return false;
    // Valida 1º dígito
    let add = 0;
    for (let i = 0; i < 9; i++) add += parseInt(cpf.charAt(i)) * (10 - i);
    let rev = 11 - (add % 11);
    if (rev === 10 || rev === 11) rev = 0;
    if (rev !== parseInt(cpf.charAt(9))) return false;
    // Valida 2º dígito
    add = 0;
    for (let i = 0; i < 10; i++) add += parseInt(cpf.charAt(i)) * (11 - i);
    rev = 11 - (add % 11);
    if (rev === 10 || rev === 11) rev = 0;
    if (rev !== parseInt(cpf.charAt(10))) return false;
    return true;
}

// --- Função de Validação das Regras da Senha ---
function validatePasswordRules(senha) {
    const rules = {
        length: senha.length >= 8,
        lowercase: /[a-z]/.test(senha),
        uppercase: /[A-Z]/.test(senha),
        number: /\d/.test(senha),
        special: /[@$!%*?&]/.test(senha)
    };

    // Atualiza a interface (UI) para mostrar quais regras são válidas/inválidas
    const updateUI = (elementId, isValid) => {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.toggle('valid', isValid);
            element.classList.toggle('invalid', !isValid);
        }
    };

    updateUI('length', rules.length);
    updateUI('lowercase', rules.lowercase);
    updateUI('uppercase', rules.uppercase);
    updateUI('number', rules.number);
    updateUI('special', rules.special);

    // Retorna true apenas se todas as regras forem atendidas
    return Object.values(rules).every(rule => rule);
}

// --- VALIDAÇÃO PRINCIPAL DO FORMULÁRIO (CHAMADA NO SUBMIT) ---
export function validateForm() {
    let isValid = true;

    // Função auxiliar para mostrar erros de forma padronizada
    const showError = (id, message) => {
        const el = $(`#${id}-error`);
        el.text(message).show();
        isValid = false;
    };

    // 1. Limpa todos os erros de cliente da tela antes de validar novamente
    $('.client-error').hide().text('');

    // 2. Valida o Nome
    const nmUsuario = $('#nmUsuario').val().trim();
    if (nmUsuario === '') {
        showError('nmUsuario', 'O nome é obrigatório.');
    }

    // 3. Valida o E-mail
    const email = $('#email').val().trim();
    if (email === '') {
        showError('email', 'O e-mail é obrigatório.');
    } else if (!/^\S+@\S+\.\S+$/.test(email)) {
        showError('email', 'Por favor, insira um e-mail válido.');
    }

    // 4. Valida a Senha
    const senha = $('#senha').val();
    if (senha === '') {
        showError('senha', 'A senha é obrigatória.');
    } else if (!validatePasswordRules(senha)) {
        $('#passwordHelpBlock').show();
        showError('senha', 'A senha não atende a todos os critérios.');
    }

    // 5. Valida o CPF
    const cpf = $('#CPF').val();
    if (cpf === '' || cpf.replace(/[^\d]/g, '').length === 0) {
        showError('cpf', 'O CPF é obrigatório.');
    } else if (!validarCPF(cpf)) {
        showError('cpf', 'O CPF é inválido.');
    }

    // 6. Valida a Foto (AGORA OBRIGATÓRIA)
    const fotoInput = document.getElementById('fotoA');
    if (fotoInput.files.length === 0) {
        showError('fotoA', 'Por favor, selecione uma foto.');
    } else {
        // Se houver um arquivo, continua validando o tipo e tamanho
        const file = fotoInput.files[0];
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
        const maxSize = 5 * 1024 * 1024; // 5MB

        if (!allowedTypes.includes(file.type)) {
            showError('fotoA', 'Tipo de arquivo não suportado. Use JPG, PNG ou GIF.');
        } else if (file.size > maxSize) {
            showError('fotoA', 'A imagem é muito grande. Máximo 5MB.');
        }
    }

    return isValid;
}


// --- EFEITOS DE FEEDBACK EM TEMPO REAL ---
$(document).ready(function() {

    // --- Nome ---
    const nomeInput = $('#nmUsuario');
    nomeInput.on('input', function() {
        const valor = $(this).val();
        const valorFiltrado = valor.replace(/[^A-Za-záàâãéèêíïóôõöúçÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇ ]/g, '');
        if (valor !== valorFiltrado) {
            $(this).val(valorFiltrado);
        }
    });
    nomeInput.on('focus', function() {
        $('#nmUsuario-error').hide();
    });

    // --- Email ---
    $('#email').on('focus', function() {
        $('#email-error').hide();
    });

    // --- Senha ---
    const senhaInput = $('#senha');
    const passwordRulesBlock = $('#passwordHelpBlock');
    passwordRulesBlock.hide();
    senhaInput.on('focus', function() {
        $('#senha-error').hide();
        passwordRulesBlock.show();
    });
    senhaInput.on('blur', function() {
        const senha = $(this).val();
        if (validatePasswordRules(senha) || senha === '') {
            passwordRulesBlock.hide();
        }
    });
    senhaInput.on('input', function() {
        $('#senha-server-error').hide();
        validatePasswordRules($(this).val());
    });

    // --- CPF ---
    const cpfInput = $('#CPF');
    cpfInput.mask('000.000.000-00');
    cpfInput.on('focus', function() {
        $('#cpf-error').hide();
    });
    cpfInput.on('blur', function() {
        const cpf = $(this).val();
        if (cpf.replace(/[^\d]/g, '').length > 0) {
            if (!validarCPF(cpf)) {
                $('#cpf-error').text('CPF inválido.').show();
            }
        }
    });

    // --- Campo de Foto ---
    const fotoInput = $('#fotoA');
    const uploadPrompt = $('#upload-prompt');
    const previewContainer = $('#preview-container');
    const imagePreview = $('#image-preview');
    const fotoAError = $('#fotoA-error');
    const removePhotoButton = $('#remove-photo-btn');

    fotoInput.on('change', function() {
        const file = this.files[0];
        if (file) {
            fotoAError.hide();
            const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
            const maxSize = 5 * 1024 * 1024; // 5MB
            if (!allowedTypes.includes(file.type) || file.size > maxSize) {
                fotoAError.text(file.size > maxSize ? 'Imagem excede 5MB.' : 'Tipo de arquivo inválido.').show();
                removePhotoButton.click();
                return;
            }
            const reader = new FileReader();
            reader.onload = function(e) {
                imagePreview.attr('src', e.target.result);
                uploadPrompt.hide();
                previewContainer.show();
            };
            reader.readAsDataURL(file);
        }
    });
    removePhotoButton.on('click', function(e) {
        e.stopPropagation();
        fotoInput.val('');
        previewContainer.hide();
        uploadPrompt.show();
        fotoAError.hide();
    });
    const fileUploadLabel = $('.file-upload-label');
    fileUploadLabel.on('dragover', (e) => { e.preventDefault(); e.stopPropagation(); fileUploadLabel.addClass('is-dragover'); });
    fileUploadLabel.on('dragleave', (e) => { e.preventDefault(); e.stopPropagation(); fileUploadLabel.removeClass('is-dragover'); });
    fileUploadLabel.on('drop', (e) => {
        e.preventDefault();
        e.stopPropagation();
        fileUploadLabel.removeClass('is-dragover');
        const files = e.originalEvent.dataTransfer.files;
        if (files.length > 0) {
            fotoInput[0].files = files;
            fotoInput.trigger('change');
        }
    });
});