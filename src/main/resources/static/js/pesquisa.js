const campoPesquisa = document.getElementById("pesquisa");
const div = document.getElementById("resultadosPesquisa");

let timeoutPesquisa;
let timeoutInteracao;

if (campoPesquisa && div) {
    campoPesquisa.addEventListener("input", function () {
        const titulo = this.value.trim();

        clearTimeout(timeoutPesquisa);
        clearTimeout(timeoutInteracao);

        if (titulo.length < 2) {
            div.innerHTML = "";
            div.style.display = "none";
            return;
        }

        // Verifica de forma segura se o botão de login existe em qualquer parte da página
        const botaoLogin = document.getElementById('loginbotao') || document.querySelector('a[href="/login"]');

        if (botaoLogin) {
            div.style.display = "block";
            div.innerHTML = `
                <div style="padding: 16px; text-align: center; color: #333; font-size: 14px; background: #fff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); border: 1px solid #e2e8f0;">
                    <p style="margin: 0 0 10px 0; font-weight: 600; color: #1e293b;">🔒 Faça login para pesquisar e ver os livros disponíveis.</p>
                    <a href="/login" style="display: inline-block; background: #007BFF; color: white; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 13px;">
                        Fazer login agora
                    </a>
                </div>
            `;
            return; // Para aqui e não chama o servidor!
        }

        // Feedback visual imediato enquanto aguarda o debounce
        div.style.display = "block";
        div.innerHTML = `
            <p style="padding: 12px; text-align: center; color: #6c63ff; font-weight: 500; margin: 0;">
                <i class="fas fa-spinner fa-spin" style="margin-right: 6px;"></i>
                Buscando livros...
            </p>
        `;

        timeoutPesquisa = setTimeout(() => {
            pesquisar(titulo);
        }, 600);
    });

    document.addEventListener("click", function (event) {
        if (!campoPesquisa.contains(event.target) && !div.contains(event.target)) {
            div.style.display = "none";
        }
    });

    campoPesquisa.addEventListener("focus", function () {
        if (div.innerHTML.trim() !== "") {
            div.style.display = "block";
        }
    });
}

function pesquisar(titulo) {
    fetch(`/pesquisar?titulo=${encodeURIComponent(titulo)}`)
        .then(res => {
            // Se o servidor retornar redirecionamento ou HTML (por falta de autenticação)
            const contentType = res.headers.get("content-type");
            if (!res.ok || (contentType && contentType.includes("text/html"))) {
                throw new Error("Faça login para continuar.");
            }
            return res.json();
        })
        .then(anuncios => {
            div.innerHTML = "";
            div.style.display = "block";

            if (anuncios.length === 0) {
                div.innerHTML = "<p style='padding: 12px; text-align: center; color: #666; margin: 0;'>Nenhum livro encontrado</p>";
            } else {
                clearTimeout(timeoutInteracao);

                timeoutInteracao = setTimeout(() => {
                    if (campoPesquisa.value.trim() === titulo) {
                        registrarInteracaoPesquisa(titulo);
                    }
                }, 2000);

                anuncios.forEach(anuncio => {
                    const link = document.createElement("a");
                    link.href = "/anuncios/" + anuncio.id;

                    const capa = document.createElement("img");
                    capa.src = anuncio.capa;
                    capa.className = "livro-capa";
                    capa.alt = "Capa do livro " + anuncio.titulo;

                    const texto = document.createElement("p");
                    texto.innerText = anuncio.titulo;

                    const vendedor = document.createElement("img");
                    vendedor.src = anuncio.fotoPerfil;
                    vendedor.className = "perfil-foto";
                    vendedor.alt = "Foto do usuário";

                    link.appendChild(capa);
                    link.appendChild(texto);
                    link.appendChild(vendedor);

                    div.appendChild(link);
                });
            }
        })
        .catch(erro => {
            div.style.display = "block";
            div.innerHTML = `
                <div style="padding: 16px; text-align: center; color: #333; font-size: 14px; background: #fff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); border: 1px solid #e2e8f0;">
                    <p style="margin: 0 0 10px 0; font-weight: 600; color: #dc2626;">🔒 Faça login para pesquisar e ver os livros.</p>
                    <a href="/login" style="display: inline-block; background: #007BFF; color: white; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 13px;">
                        Fazer login agora
                    </a>
                </div>
            `;
        });
}

function registrarInteracaoPesquisa(termo) {
    fetch(`/pesquisar/interacao?termo=${encodeURIComponent(termo)}`, {
        method: "POST"
    }).catch(() => {});
}