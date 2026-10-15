(function () {
  "use strict";

  var root = document.documentElement;
  var storageKey = "petclinic-theme";

  function storedTheme() {
    try {
      return window.localStorage.getItem(storageKey);
    }
    catch (error) {
      return null;
    }
  }

  function saveTheme(theme) {
    try {
      window.localStorage.setItem(storageKey, theme);
    }
    catch (error) {
      // Theme still applies for the current page when storage is unavailable.
    }
  }

  function preferredTheme() {
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }

  function requestedTheme() {
    var theme = new URLSearchParams(window.location.search).get("theme");
    return theme === "dark" || theme === "light" ? theme : null;
  }

  function applyTheme(theme, persist) {
    root.setAttribute("data-theme", theme);
    if (persist) {
      saveTheme(theme);
    }
    var toggle = document.getElementById("theme-toggle");
    if (toggle) {
      var dark = theme === "dark";
      toggle.setAttribute("aria-pressed", String(dark));
      toggle.setAttribute("aria-label", dark ? toggle.dataset.lightLabel : toggle.dataset.darkLabel);
    }
  }

  applyTheme(requestedTheme() || storedTheme() || preferredTheme(), Boolean(requestedTheme()));

  document.addEventListener("DOMContentLoaded", function () {
    var themeToggle = document.getElementById("theme-toggle");
    if (themeToggle) {
      applyTheme(root.getAttribute("data-theme") || preferredTheme(), false);
      themeToggle.addEventListener("click", function () {
        var nextTheme = root.getAttribute("data-theme") === "dark" ? "light" : "dark";
        applyTheme(nextTheme, true);
      });
    }

    document.querySelectorAll("[data-list-filter]").forEach(function (input) {
      var container = document.querySelector(input.dataset.listFilter);
      var emptyState = document.querySelector(input.dataset.emptyTarget);
      if (!container) {
        return;
      }
      var items = Array.from(container.children);
      if (emptyState) {
        emptyState.setAttribute("aria-live", "polite");
      }
      input.addEventListener("input", function () {
        var query = input.value.trim().toLocaleLowerCase();
        var visible = 0;
        items.forEach(function (item) {
          var text = (item.dataset.filterText || item.textContent).toLocaleLowerCase();
          var matches = !query || text.indexOf(query) !== -1;
          item.hidden = !matches;
          if (matches) {
            visible += 1;
          }
        });
        if (emptyState) {
          emptyState.hidden = visible !== 0;
        }
      });
    });

    document.querySelectorAll(".toast-message").forEach(function (message) {
      window.setTimeout(function () {
        message.classList.add("is-hiding");
        window.setTimeout(function () {
          message.hidden = true;
        }, 200);
      }, 4000);
    });
  });
})();
