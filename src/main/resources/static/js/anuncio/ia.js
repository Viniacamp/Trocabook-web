export function inicializarIaDescricao() {
    const btnGerar = document.getElementById("btnGerarDescricaoIa");
    const textareaDescricao = document.getElementById("descricao");
    const erroIa = document.getElementById("erroIa");

    if (!btnGerar || !textareaDescricao) return;

    const extrairUnicos = (seletor) => {
        const itens = Array.from(document.querySelectorAll(seletor))
            .map(el => el.textContent.replace("×", "").trim())
            .filter(Boolean);
        return [...new Set(itens)].join(", ");
    };

    btnGerar.addEventListener("click", async () => {
        const titulo = document.getElementById("titulo")?.value?.trim() || "";
        const modo = document.getElementById("modoCadastro")?.value || "";
        const tipoNegociacao = document.getElementById("tipoNegociacao")?.value || "";
        const rascunho = textareaDescricao.value.trim();

        const autores = modo === "MANUAL"
            ? (document.getElementById("autoresManual")?.value?.trim() || "")
            : extrairUnicos("#autores .tag");

        const categorias = modo === "MANUAL"
            ? extrairUnicos("#categoriasSelecionadas span, #categoriasSelecionadas .tag")
            : extrairUnicos("#categorias .tag");

        if (!titulo) {
            erroIa.textContent = "Informe ou busque o título do livro antes de gerar a descrição com IA.";
            erroIa.style.display = "block";
            return;
        }

        erroIa.style.display = "none";
        const textoOriginalBotao = btnGerar.innerHTML;
        btnGerar.disabled = true;
        btnGerar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Gerando...';

        try {
            const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

            const response = await fetch("/api/ia/gerar-descricao", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(csrfToken ? { "X-CSRF-TOKEN": csrfToken } : {})
                },
                body: JSON.stringify({
                    titulo,
                    autores,
                    categorias,
                    tipoNegociacao,
                    rascunho
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.erro || "Erro ao gerar descrição.");
            }

            textareaDescricao.value = data.descricao;
            textareaDescricao.focus();
        } catch (err) {
            erroIa.textContent = err.message;
            erroIa.style.display = "block";
        } finally {
            btnGerar.disabled = false;
            btnGerar.innerHTML = textoOriginalBotao;
        }
    });
}