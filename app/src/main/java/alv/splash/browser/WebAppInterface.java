package alv.splash.browser;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.lang.ref.WeakReference;

public class WebAppInterface {

    private WeakReference<StartWorking> startWorkingWeakReference;

    private Context mContext;

    // Constructor baru untuk StartWorking
    public WebAppInterface(StartWorking activity) {
        this.startWorkingWeakReference = new WeakReference<>(activity);
        Log.i("WebAppInterface", "WebAppInterface Initialized");
    }

    // Constructor lama untuk backward compatibility jika diperlukan
    @Deprecated
    public WebAppInterface(Context context) {
        Log.i("WebAppInterface", "WebAppInterface Initialized");
        this.mContext = context;
    }

    public String updateTitleEarning(String title) {
        thisTitleEarning = title;
        return thisTitleEarning;
    }

    private String thisTitleEarning = "Money Earning";


    public String removeSpacesStringBuilder(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isWhitespace(input.charAt(i))) {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }

    public String makeSingleLine(String input) {
        if (input == null) return "";
        return input.replaceAll("\\s*[\r\n]+\\s*", " ").trim();
    }

    public String scriptInjectDataTitle = """
                    (function() {
            let titleObserver = null;
                        let targetButton = null;
                        let mainObserver = null;
                        let captchaObserver = null;
                        function startObserving() {
                            observeTitle();
                        }
                        function observeTitle() {
                            console.log('Starting title observer');
                            cleanupObservers();
            
                            function checkTitle() {
                                const title = document.title;
                                console.log('Current title:', title);
            
                                if (title && title.includes('""" + thisTitleEarning + """
            ')) {
            console.log('Title: """ + thisTitleEarning + """
                                 detected');
                                 AndroidInterface.elementFound('page-title');
            
                                 observeWorkArea();
                             } else {
                                 AndroidInterface.elementNotFound('page-title');
                             }
                         }
            
                         checkTitle();
            
                         titleObserver = new MutationObserver(function() {
                             checkTitle();
                         });
            
                         const titleElement = document.querySelector('title');
                         if (titleElement) {
                             titleObserver.observe(titleElement, { childList: true, characterData: true, subtree: true });
                             console.log('Title observer started');
                         } else {
            
                             titleObserver.observe(document.querySelector('head'), { childList: true, subtree: true });
                             console.log('Head observer started (waiting for title)');
                         }
                     }
            
                     function observeWorkArea() {
                         const targetNode = document.querySelector('.work-area-wrap');
                         if (!targetNode) {
                             AndroidInterface.mutationFailure('work-area-wrap', 'Target node not found');
            
                             setTimeout(observeWorkArea, 1000);
                             return;
                         }
                         console.log('Mutation Observer initialized for work-area-wrap');
                         AndroidInterface.elementFound('work-area-wrap');
            
                         const config = {
                             childList: true,
                             subtree: true,
                             attributes: true,
                             attributeFilter: ['style']
                         };
            
                         const callback = function(mutationsList) {
                             console.log('Mutation detected: ', mutationsList.length + ' changes');
                             try {
                                 checkAndCaptureElements();
                             } catch (e) {
                                 AndroidInterface.mutationFailure('observer-callback', e.message);
                             }
                         };
            
                         if (mainObserver) {
                             mainObserver.disconnect();
                         }
            
                         mainObserver = new MutationObserver(callback);
                         mainObserver.observe(targetNode, config);
            
                         checkAndCaptureElements();
                     }
            
                     function cleanupObservers() {
            
            if(titleObserver){
                             console.log('Disconeting title observer');
                             titleObserver.disconnect();
                             titleObserver=null;
                         }
            
                         if (mainObserver) {
                             console.log('Disconnecting main observer');
                             mainObserver.disconnect();
                             mainObserver = null;
                         }
            
                         if (captchaObserver) {
                             console.log('Disconnecting captcha observer');
                             captchaObserver.disconnect();
                             captchaObserver = null;
                         }
                     }
            
                     function findSubmitButton() {
            
                         if (targetButton && document.body.contains(targetButton)) {
                             return targetButton;
                         }
            
                         const buttons = document.querySelectorAll('button.btn.btn-default');
            
                         for (const button of buttons) {
            
                             const span = button.querySelector('span.label');
                             if (span && span.textContent.trim() === 'Submit') {
                                 targetButton = button;
                                 AndroidInterface.elementFound('submit-button');
                                 console.log('Submit button found');
                                 return button;
                             }
                         }
                         AndroidInterface.elementNotFound('submit-button');
                         return null;
                     }
                     function checkAndCaptureElements() {
                         const captcha = document.querySelector('.captcha-image');
                         const input = document.querySelector('.inp-dft');
                         const submitButton = findSubmitButton();
                         if (captcha) {
                             AndroidInterface.elementFound('captcha-image');
                             console.log('Captcha element found');
                             try {
                                 const bgImage = window.getComputedStyle(captcha).backgroundImage;
                                 if (bgImage) {
                                     let fullDataUrl = bgImage;
                                     if (bgImage.includes('url(')) {
                                         fullDataUrl = bgImage.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                     }
                                     console.log('Captcha dataURL captured');
                                     AndroidInterface.onCaptchaCaptured(fullDataUrl);
                                     if (captchaObserver) {
                                         captchaObserver.disconnect();
                                     }
                                     captchaObserver = new MutationObserver(function(mutations) {
                                         const newBg = window.getComputedStyle(captcha).backgroundImage;
                                         if (newBg !== bgImage) {
                                             console.log('Captcha image changed');
                                             let newUrl = newBg;
                                             if (newBg.includes('url(')) {
                                                 newUrl = newBg.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                             }
                                             AndroidInterface.onCaptchaCaptured(newUrl);
                                         }
                                     });
                                     captchaObserver.observe(captcha, {
                                         attributes: true,
                                         attributeFilter: ['style']
                                     });
                                 } else {
                                     AndroidInterface.mutationFailure('captcha-image', 'Invalid background format');
                                 }
                             } catch (e) {
                                 AndroidInterface.mutationFailure('captcha-image', e.message);
                             }
                         } else {
                             AndroidInterface.elementNotFound('captcha-image');
                         }
                         if (input) {
                             AndroidInterface.elementFound('inp-dft');
                             console.log('Input element found');
                             try {
                                 AndroidInterface.onInputCaptured(input.value);
                                 input.removeEventListener('keydown', inputKeydownHandler);
                                 input.addEventListener('keydown', inputKeydownHandler);
                             } catch (e) {
                                 AndroidInterface.mutationFailure('inp-dft', e.message);
                             }
                         } else {
                             AndroidInterface.elementNotFound('inp-dft');
                         }
                     }
                     function inputKeydownHandler(e) {
                         if (e.key === 'Enter' || e.keyCode === 13) {
                             console.log('Input label: ', this.value);
                             AndroidInterface.onInputCaptured(this.value.trim());
            
                             const submitButton = findSubmitButton();
                             if (submitButton) {
                                 console.log('Clicking submit button');
                                 submitButton.click();
                                 /*AndroidInterface.buttonClicked('submit-button');*/
                             } else {
                                 AndroidInterface.mutationFailure('submit-button', 'Button not found when trying to click');
                             }
            
                         }
                     }
            
                     cleanupObservers();
            
                     if (document.readyState === 'loading') {
                         document.addEventListener('DOMContentLoaded', function() {
                             console.log('DOM fully loaded');
                             startObserving();
                         });
                     } else {
                         startObserving();
                     }
                 })();
            
            """;

    public String scriptInjectData = String.format("""
        if (typeof window.detectElementChangesCleanup === 'function') {
            console.log("Cleaning up previous detectElementChanges instance");
            window.detectElementChangesCleanup();
            window.detectElementChangesCleanup = null;
        }
        
        (function() {
            let titleObserver = null;
            let workAreaObserver = null;
            let thisTitleEarning = '%s';
            let lastCaptchaUrl = '';
            let throttleTimer = null;
        
            const state = {
                title: false,
                workArea: false,
                captcha: false,
                input: false,
                submitButton: false
            };
        
            function startObserving() {
                cleanupObservers();
                observeTitle();
                console.log("Element detection started (IIFE version)");
            }
        
            function observeTitle() {
                const titleElement = document.querySelector('title');
        
                function checkTitle() {
                    const title = document.title;
                    const titleMatches = title && title.includes(thisTitleEarning);
        
                    if (titleMatches && !state.title) {
                        state.title = true;
                        console.log('Title detected');
                        findWorkArea();
                    } else if (!titleMatches && state.title) {
                        state.title = false;
                        console.log('Title: not found || not match');
                        cleanupWorkAreaObserver();
                    }
                }
        
                checkTitle();
        
                if (titleElement) {
                    titleObserver = new MutationObserver(checkTitle);
                    titleObserver.observe(titleElement, { childList: true, characterData: true });
                    console.log('Title observer started');
                } else {
                    const headElement = document.querySelector('head');
                    if (headElement) {
                        titleObserver = new MutationObserver(function() {
                            if (document.querySelector('title')) {
                                titleObserver.disconnect();
                                observeTitle();
                            }
                        });
                        titleObserver.observe(headElement, { childList: true, subtree: false });
                        console.log('Head observer started (waiting for title)');
                    }
                }
            }
        
            function findWorkArea() {
                const workArea = document.querySelector('.work-area-wrap');
        
                if (!workArea) {
                    setTimeout(findWorkArea, 1000);
                    return;
                }
        
                if (!state.workArea) {
                    state.workArea = true;
                    console.log('Work area found');
                }
        
                checkElements(workArea);
        
                if (!workAreaObserver) {
                    workAreaObserver = new MutationObserver(function() {
                        if (!throttleTimer) {
                            throttleTimer = setTimeout(function() {
                                checkElements(workArea);
                                throttleTimer = null;
                            }, 200);
                        }
                    });
        
                    workAreaObserver.observe(workArea, { 
                        childList: true, 
                        subtree: true,
                        attributes: false
                    });
                    console.log('Work area observer started');
                }
            }
        
            function cleanupWorkAreaObserver() {
                if (workAreaObserver) {
                    workAreaObserver.disconnect();
                    workAreaObserver = null;
                }
        
                if (throttleTimer) {
                    clearTimeout(throttleTimer);
                    throttleTimer = null;
                }
        
                state.workArea = false;
                state.captcha = false;
                state.input = false;
                state.submitButton = false;
        
                const input = document.querySelector('.inp-dft');
                if (input) {
                    input.removeEventListener('keydown', inputKeydownHandler);
                }
            }
        
            function checkElements(workArea) {
                if (!workArea || !state.workArea) return;
        
                const captcha = workArea.querySelector('.captcha-image');
                const input = workArea.querySelector('.inp-dft');
        
                let submitButton = null;
                const buttons = workArea.querySelectorAll('button.btn.btn-default');
                for (const button of buttons) {
                    const span = button.querySelector('span');
                    if (span && span.textContent.trim() === 'Submit') {
                        submitButton = button;
                        break;
                    }
                }
        
                if (captcha) {
                    if (!state.captcha) {
                        state.captcha = true;
                        console.log('[ New Captcha found ]');
                    }
                    try {
                        const bgImage = window.getComputedStyle(captcha).backgroundImage;
                        if (bgImage && bgImage !== 'none') {
                            const newCaptchaUrl = bgImage.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                            AndroidInterface.newCaptchaFound(newCaptchaUrl);
                            lastCaptchaUrl = newCaptchaUrl;
                            console.log('Captcha [ VISIBLE ]');
                        }
                    } catch (e) {
                        console.log('Mutation failure', e.getMessage);
                    }
                } else if (state.captcha) {
                    state.captcha = false;
                    console.log('Captcha [ GONE ]');
                }
        
                if (input) {
                    if (!state.input) {
                        state.input = true
                        input.focus();
                        /*console.log('Input [ VISIBLE ]');*/
        
                        input.removeEventListener('keydown', inputKeydownHandler);
                        input.addEventListener('keydown', inputKeydownHandler);
                    }
                } else if (state.input) {
                    state.input = false;
                    /*console.log('Input [ GONE ]');*/
                }
        
                if (submitButton) {
                    if (!state.submitButton) {
                        state.submitButton = true;
                    }
                } else if (state.submitButton) {
                    state.submitButton = false;
                }
            }
        
            function inputKeydownHandler(e) {
                if (e.key === 'Enter' || e.keyCode === 13) {
                    const value = this.value.trim();
                    AndroidInterface.onCaptchaCaptured(lastCaptchaUrl);
                    console.log('Captcha URL captured');
                    AndroidInterface.onInputCaptured(value);
                    console.log('Input label: ', value);
        
                    const workArea = document.querySelector('.work-area-wrap');
                    if (workArea) {
                        const buttons = workArea.querySelectorAll('button.btn.btn-default');
                        for (const button of buttons) {
                            const span = button.querySelector('span');
                            if (span && span.textContent.trim() === 'Submit') {
                                console.log('Clicking submit button');
                                AndroidInterface.onButtonSubmitted('816aef8d8880e9681c755a8597634c3a4f568af30ce1b71d6216d807c8b88953');
                                button.click();
                                return;
                            }
                        }
                    }
        
                    console.log('Mutation failure [inputKeydownHandler]: submit btn not found');
                }
            }
        
            function cleanupObservers() {
                if (titleObserver) {
                    titleObserver.disconnect();
                    titleObserver = null;
                }
        
                cleanupWorkAreaObserver();
            }
        
            window.detectElementChangesCleanup = cleanupObservers;
        
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', startObserving);
            } else {
                startObserving();
            }
        })();
        
        window.setTextInput = function(text) {
            try {
                const input = document.querySelector(".inp-dft");
                if (input) {
                    input.value = text;
                    input.focus();
                    const event = new Event("input", { bubbles: true });
                    input.dispatchEvent(event);
                    console.log("Input label: ", text);
                    return "success";
                } else {
                    return "error: input element not found";
                }
            } catch (e) {
                return "error: " + e.message;
            }
        };
        """, thisTitleEarning);

    // Gemini
    public String scriptInjectDataGemini = """
            (function() {
                let cachedSubmitButton = null;
            
                function startObserving() {
                    const targetNode = document.querySelector('.work-area-wrap');
                    if (!targetNode) {
                        console.log('Target node .work-area-wrap not found');
                        return;
                    }
                    console.log('Mutation Observer initialized');
            
                    const config = {
                        childList: true,
                        subtree: true,
                        attributes: true,
                        attributeFilter: ['style']
                    };
            
                    const callback = function(mutationsList) {
                        console.log('Mutation detected: ', mutationsList.length + ' changes');
                        try {
                            checkAndCaptureElements();
                        } catch (e) {
                            console.log('Mutation failure: ', e.message);
                        }
                    };
            
                    const observer = new MutationObserver(callback);
                    observer.observe(targetNode, config);
                    checkAndCaptureElements();
                }
            
                function findSubmitButton() {
                    if (cachedSubmitButton) {
                        return cachedSubmitButton;
                    }
            
                    const buttons = document.querySelectorAll('button.btn');
                    for (const button of buttons) {
                        const spanText = button.querySelector('span.label')?.textContent;
                        if (spanText === 'Submit') {
                            cachedSubmitButton = button;
                            return button;
                        }
                    }
            
                    return null;
                }
            
                function checkAndCaptureElements() {
                    const captcha = document.querySelector('.captcha-image');
                    const input = document.querySelector('.inp-dft');
            
                    if (captcha) {
                        console.log('Captcha element found');
                        try {
                            const bgImage = window.getComputedStyle(captcha).backgroundImage;
                            if (bgImage) {
                                let fullDataUrl = bgImage;
                                if (bgImage.includes('url(')) {
                                    fullDataUrl = bgImage.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                }
                                console.log('Captcha dataURL captured');
                                AndroidInterface.onCaptchaCaptured(fullDataUrl);
            
                                new MutationObserver(function(mutations) {
                                    const newBg = window.getComputedStyle(captcha).backgroundImage;
                                    if (newBg !== bgImage) {
                                        console.log('Captcha image changed');
                                        let newUrl = newBg;
                                        if (newBg.includes('url(')) {
                                            newUrl = newBg.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                        }
                                        AndroidInterface.onCaptchaCaptured(newUrl);
                                    }
                                }).observe(captcha, {
                                    attributes: true,
                                    attributeFilter: ['style']
                                });
                            } else {
                                console.log('Invalid background format');
                            }
                        } catch (e) {
                            console.log('Mutation failure: ', e.message);
                        }
                    } else {
                        console.log('Captcha element not found');
                    }
            
                    if (input) {
                        input.focus();
                        console.log('Input element found');
                        try {
                            input.addEventListener('keydown', inputKeydownHandler);
                        } catch (e) {
                            console.log('Mutation failure: ', e.message);
                        }
                    } else {
                        console.log('Input element not found');
                    }
                }
            
                function inputKeydownHandler(e) {
                    if (e.key === 'Enter' || e.keyCode === 13) {
                        console.log('Input label: ', this.value);
                        AndroidInterface.onInputCaptured(this.value.trim());
                        const submitButton = findSubmitButton();
                        if (submitButton) {
                            console.log('Clicking submit button');
                            submitButton.click();
                        } else {
                            console.log('Submit button not found when trying to click');
                        }
                    }
                }
            
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', function() {
                        console.log('DOM fully loaded');
                        startObserving();
                    });
                } else {
                    startObserving();
                }
            })();
            """;


    // Menggunakan Java text block (Java 17) untuk menyimpan JavaScript [Revisi]
    public String scriptInjectDataOri = """
            (function() {
            let cachedSubmitButton = null;
                function startObserving() {
                    const targetNode = document.querySelector('.work-area-wrap');
                    if (!targetNode) {
                        console.log('Target node .work-area-wrap not found');
                        return;
                    }
                    console.log('Mutation Observer initialized');
            
                    const config = {
                        childList: true,
                        subtree: true,
                        attributes: true,
                        attributeFilter: ['style']
                    };
            
                    const callback = function(mutationsList) {
                        console.log('Mutation detected: ', mutationsList.length + ' changes');
                        try {
                            checkAndCaptureElements();
                        } catch (e) {
                            console.log('Mutation failure: ', e.message);
                        }
                    };
            
                    const observer = new MutationObserver(callback);
                    observer.observe(targetNode, config);
                    checkAndCaptureElements();
                }
            
            function findSubmitButton() {
              if (cachedSubmitButton) {
                return cachedSubmitButton;
              }
            
              const buttons = document.querySelectorAll('button.btn');
              for (const button of buttons) {
                const spanText = button.querySelector('span.label')?.textContent;
                if (spanText === 'Submit') {
                  cachedSubmitButton = button;
                  return button;
                }
              }
            
              return null;
            }
            
                function checkAndCaptureElements() {
                    const captcha = document.querySelector('.captcha-image');
                    const input = document.querySelector('.inp-dft');
            
                    if (captcha) {
                        console.log('Captcha element found');
                        try {
                            const bgImage = window.getComputedStyle(captcha).backgroundImage;
                            if (bgImage) {
                                let fullDataUrl = bgImage;
                                if (bgImage.includes('url(')) {
                                    fullDataUrl = bgImage.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                }
                                console.log('Captcha dataURL captured');
                                AndroidInterface.onCaptchaCaptured(fullDataUrl);
            
                                new MutationObserver(function(mutations) {
                                    const newBg = window.getComputedStyle(captcha).backgroundImage;
                                    if (newBg !== bgImage) {
                                        console.log('Captcha image changed');
                                        let newUrl = newBg;
                                        if (newBg.includes('url(')) {
                                            newUrl = newBg.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                        }
                                        AndroidInterface.onCaptchaCaptured(newUrl);
                                    }
                                }).observe(captcha, {
                                    attributes: true,
                                    attributeFilter: ['style']
                                });
                            } else {
                            console.log('Invalid background format');
                            }
                        } catch (e) {
                            console.log('Mutation failure: ', e.message);
                        }
                    } else {
                        console.log('Captcha element not found');
                    }
            
                    if (input) {
            			input.focus();
                        console.log('Input element found');
                        try {
            				input.addEventListener('keydown', inputKeydownHandler);
                        } catch (e) {
                            console.log('Mutation failure: ', e.message);
                        }
                    } else {
                        console.log('Input element not found');
                    }
                }
            
                function inputKeydownHandler(e) {
                              if (e.key === 'Enter' || e.keyCode === 13) {
                                console.log('Input label: ', this.value);
                                AndroidInterface.onInputCaptured(this.value.trim());
                                const submitButton = findSubmitButton();
                                if (submitButton) {
                                  console.log('Clicking submit button');
                                  submitButton.click();
                                } else {
                                  console.log('Submit button not found when trying to click');
                                }
                              }
                            }
            
            
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', function() {
                        console.log('DOM fully loaded');
                        startObserving();
                    });
                } else {
                    startObserving();
                }
            })();
            """;

    public String scriptInjectDataBackup = "(function() {" + "function startObserving() {" + "    const targetNode = document.querySelector('.work-area-wrap');" + "    if (!targetNode) {" + "        AndroidInterface.mutationFailure('work-area-wrap', 'Target node not found');" + "        return;" + "    }" + "    console.log('MutationObserver initialized');" + "    AndroidInterface.elementFound('work-area-wrap');" + "    const config = { " + "        childList: true," + "        subtree: true," + "        attributes: true," + "        attributeFilter: ['style']" + "    };" + "    const callback = function(mutationsList) {" + "        console.log('Mutation detected:', mutationsList.length + ' changes');" + "        try {" + "            checkAndCaptureElements();" + "        } catch (e) {" + "            AndroidInterface.mutationFailure('observer-callback', e.message);" + "        }" + "    };" + "    const observer = new MutationObserver(callback);" + "    observer.observe(targetNode, config);" + "    checkAndCaptureElements();" + "}" + "function checkAndCaptureElements() {" + "    const captcha = document.querySelector('.captcha-image');" + "    const input = document.querySelector('.inp-dft');" + "    if (captcha) {" + "        AndroidInterface.elementFound('captcha-image');" + "        console.log('Captcha element found');" + "        try {" + "            const bgImage = window.getComputedStyle(captcha).backgroundImage;" + "            if (bgImage) {" + "                let fullDataUrl = bgImage;" + "                if (bgImage.includes('url(')) {" + "                    fullDataUrl = bgImage.replace(/^url\\(['\"]?/, '').replace(/['\"]?\\)$/, '');" + "                }" + "                console.log('Captcha data URL captured');" + "                AndroidInterface.onCaptchaCaptured(fullDataUrl);" + "                new MutationObserver(function(mutations) {" + "                    const newBg = window.getComputedStyle(captcha).backgroundImage;" + "                    if (newBg !== bgImage) {" + "                        console.log('Captcha image changed');" + "                        let newUrl = newBg;" + "                        if (newBg.includes('url(')) {" + "                            newUrl = newBg.replace(/^url\\(['\"]?/, '').replace(/['\"]?\\)$/, '');" + "                        }" + "                        AndroidInterface.onCaptchaCaptured(newUrl);" + "                    }" + "                }).observe(captcha, { attributes: true, attributeFilter: ['style'] });" + "            } else {" + "                AndroidInterface.mutationFailure('captcha-image', 'Invalid background format');" + "            }" + "        } catch (e) {" + "            AndroidInterface.mutationFailure('captcha-image', e.message);" + "        }" + "    } else {" + "        AndroidInterface.elementNotFound('captcha-image');" + "    }" + "    if (input) {" + "        AndroidInterface.elementFound('inp-dft');" + "        console.log('Input element found');" + "        try {" + "            AndroidInterface.onInputCaptured(input.value);" + "    input.addEventListener('keydown', function(e) {" + "        if (e.key === 'Enter' || e.keyCode === 13) {" + "            console.log('Input label: ', input.value);" + "            AndroidInterface.onInputCaptured(input.value.trim());" + "             }" + "            });" + "        } catch (e) {" + "            AndroidInterface.mutationFailure('inp-dft', e.message);" + "        }" + "    } else {" + "        AndroidInterface.elementNotFound('inp-dft');" + "    }" + "}" + "if (document.readyState === 'loading') {" + "    document.addEventListener('DOMContentLoaded', function() {" + "        console.log('DOM fully loaded');" + "        startObserving();" + "    });" + "} else {" + "    startObserving();" + "}" + "})();";

    public String autoInputLogin = """
            
            	(function() {
                try {
                    console.log("%c[Security Test] Memulai skrip injeksi input...", "color: blue; font-weight: bold");
                    console.log(`[Info] User Agent: ${navigator.userAgent}`);
                    console.log(`[Info] Timestamp: ${new Date().toISOString()}`);
            
                    const usernameSelectors = ['input[name="login"]', 'input[type="text"][id*="user"]', 'input[id*="email"]', 'input[name="username"]'];
                    const passwordSelectors = ['input[name="password"]', 'input[type="password"]'];
            
                    let usernameInput = null;
                    let passwordInput = null;
            
                    for (const selector of usernameSelectors) {
                        const found = document.querySelector(selector);
                        if (found) {
                            usernameInput = found;
                            console.log(`[+] Element username ditemukan dengan selector: ${selector}`);
                            break;
                        }
                    }
            
                    for (const selector of passwordSelectors) {
                        const found = document.querySelector(selector);
                        if (found) {
                            passwordInput = found;
                            console.log(`[+] Element password ditemukan dengan selector: ${selector}`);
                            break;
                        }
                    }
            
                    if (!usernameInput) console.log("%c[-] Gagal menemukan element username dengan semua selector", "color: red");
                    if (!passwordInput) console.log("%c[-] Gagal menemukan element password dengan semua selector", "color: red");
            
                    console.log("[Security Analysis] Memeriksa atribut keamanan input...");
            
                    if (usernameInput && passwordInput) {
            
                        const securityCheck = (element, name) => {
                            console.log(`[Pre-Inject] Analisis elemen ${name}:`);
                            console.log(`- readOnly: ${element.readOnly}`);
                            console.log(`- disabled: ${element.disabled}`);
                            console.log(`- autocomplete: ${element.getAttribute('autocomplete') || 'tidak diatur'}`);
                            console.log(`- maxLength: ${element.maxLength}`);
                            console.log(`- pattern: ${element.getAttribute('pattern') || 'tidak diatur'}`);
                            console.log(`- has event listeners: ${element.onchange || element.oninput || element.onkeydown ? 'Ya' : 'Tidak'}`);
            
                            const parentForm = element.closest('form');
                            if (parentForm) {
                                console.log(`- Parent form action: ${parentForm.action || 'tidak diatur'}`);
                                console.log(`- Form novalidate: ${parentForm.noValidate}`);
                            }
            
                            const isInIframe = window !== window.top;
                            console.log(`- Dalam iframe: ${isInIframe}`);
                        };
            
                        securityCheck(usernameInput, 'username');
                        securityCheck(passwordInput, 'password');
            
                        const originalUsername = usernameInput.value;
                        const originalPassword = passwordInput.value;
                        console.log(`[Original Values] Username: ${originalUsername || '(kosong)'}, Password: ${originalPassword ? '*****' : '(kosong)'}`);
            
                        const testInjection = (element, value, name) => {
                            console.log(`%c[!] Mencoba mengisi nilai ${name}...`, "color: orange; font-weight: bold");
            
                            try {
            
                                const originalDescriptor = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value');
            
                                console.log(`[Attempt 1] Menggunakan direct assignment (element.value = ${value})`);
                                const originalVal = element.value;
                                element.value = value;
            
                                if (element.value === value) {
                                    console.log(`%c[+] ${name} berhasil di-set dengan direct assignment`, "color: green");
                                } else {
                                    console.log(`%c[-] ${name} gagal di-set dengan direct assignment`, "color: red");
                                    console.log(`[Detected] Nilai setelah direct assignment: "${element.value}"`);
                                    console.log(`[Reason] Kemungkinan ada getter/setter yang di-override atau event handler yang mereset nilai`);
            
                                    const currentDescriptor = Object.getOwnPropertyDescriptor(element, 'value');
                                    if (currentDescriptor && (currentDescriptor.get !== originalDescriptor.get || currentDescriptor.set !== originalDescriptor.set)) {
                                        console.log("%c[Alert] Terdeteksi getter/setter kustom pada elemen input!", "color: red; font-weight: bold");
                                    }
                                }
            
                                console.log(`[Attempt 2] Menggunakan setAttribute`);
                                element.setAttribute('value', value);
                                if (element.value === value) {
                                    console.log(`%c[+] ${name} berhasil di-set dengan setAttribute`, "color: green");
                                } else {
                                    console.log(`%c[-] ${name} gagal di-set dengan setAttribute`, "color: red");
                                    console.log(`[Detected] Nilai setelah setAttribute: "${element.value}"`);
                                }
            
                                console.log(`[Attempt 3] Menggunakan simulasi event`);
                                try {
                                    element.dispatchEvent(new Event('focus'));
                                    element.value = value;
                                    element.dispatchEvent(new Event('input', { bubbles: true }));
                                    element.dispatchEvent(new Event('change', { bubbles: true }));
            
                                    if (element.value === value) {
                                        console.log(`%c[+] ${name} berhasil di-set dengan simulasi event`, "color: green");
                                    } else {
                                        console.log(`%c[-] ${name} gagal di-set dengan simulasi event`, "color: red");
                                        console.log(`[Detected] Nilai setelah simulasi event: "${element.value}"`);
                                    }
                                } catch (eventError) {
                                    console.log(`%c[-] Error saat mencoba simulasi event: ${eventError.message}`, "color: red");
                                }
            
                                return element.value === value;
                            } catch (error) {
                                console.error(`%c[Error] Gagal mengatur nilai ${name}: ${error.message}`, "color: red");
                                return false;
                            }
                        };
            
                        const usernameSuccess = testInjection(usernameInput, 'nusantara1445h', 'username');
                        const passwordSuccess = testInjection(passwordInput, '13mar2024', 'password');
            
                        console.log("%c[Post-Inject] Memverifikasi status elemen setelah injeksi...", "color: blue");
            
                        if (usernameInput.readOnly) console.log("%c[!] Username input di-mark readOnly setelah injeksi", "color: orange");
                        if (usernameInput.disabled) console.log("%c[!] Username input dinonaktifkan setelah injeksi", "color: orange");
                        if (passwordInput.readOnly) console.log("%c[!] Password input di-mark readOnly setelah injeksi", "color: orange");
                        if (passwordInput.disabled) console.log("%c[!] Password input dinonaktifkan setelah injeksi", "color: orange");
            
                        if (passwordInput.type !== 'password') {
                            console.log(`%c[!] Tipe password diubah ke: ${passwordInput.type}`, "color: orange");
                        }
            
                        const attributeCheck = (element, name, originalValue) => {
                            const currentClasses = element.className;
                            const style = window.getComputedStyle(element);
                            console.log(`[Post-Inject] ${name} status:`);
                            console.log(`- Classes: ${currentClasses}`);
                            console.log(`- Visibility: ${style.visibility}`);
                            console.log(`- Display: ${style.display}`);
                            console.log(`- Opacity: ${style.opacity}`);
            
                            if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') {
                                console.log(`%c[Alert] ${name} input mungkin disembunyikan setelah injeksi!`, "color: red; font-weight: bold");
                            }
            
                            if (!originalValue.includes('error') && currentClasses.includes('error')) {
                                console.log(`%c[Alert] ${name} input memiliki class 'error' setelah injeksi!`, "color: red");
                            }
                        };
            
                        attributeCheck(usernameInput, 'Username', usernameInput.className || '');
                        attributeCheck(passwordInput, 'Password', passwordInput.className || '');
            
                        console.log("[Security Check] Memeriksa browser anti-tampering...");
                        if (window.length !== window.history.length) {
                            console.log("%c[Alert] Kemungkinan deteksi anti-tampering terdeteksi", "color: red");
                        }
            
                        const allInputs = document.querySelectorAll('input');
                        const hiddenInputs = Array.from(allInputs).filter(input => {
                            const style = window.getComputedStyle(input);
                            return (style.display === 'none' || style.visibility === 'hidden' || input.type === 'hidden');
                        });
            
                        if (hiddenInputs.length > 0) {
                            console.log("%c[Alert] Terdeteksi input tersembunyi, kemungkinan honeypot:", "color: orange");
                            hiddenInputs.forEach(input => {
                                console.log(`- ${input.name || input.id || 'unnamed'}: type=${input.type}, value=${input.value}`);
                            });
                        }
            
                        const timeouts = [100, 500, 2000, 5000];
                        timeouts.forEach(timeout => {
                            setTimeout(() => {
                                console.log(`%c[Time Check ${timeout}ms] Memverifikasi nilai setelah ${timeout}ms...`, "color: blue");
            
                                const currentUsernameValue = usernameInput.value;
                                const currentPasswordValue = passwordInput.value;
                                const usernameSame = currentUsernameValue === 'nusantara1445h';
                                const passwordSame = currentPasswordValue === '13mar2024';
            
                                console.log(`Username setelah ${timeout}ms: ${usernameSame ? "Masih valid" : `Nilai berubah ke "${currentUsernameValue}"`}`);
                                console.log(`Password setelah ${timeout}ms: ${passwordSame ? "Masih valid" : "Nilai berubah"}`);
            
                                if (!usernameSame || !passwordSame) {
                                    console.log("%c[Alert] Nilai diubah setelah injeksi! Kemungkinan ada watch/observer JavaScript", "color: red; font-weight: bold");
                                }
            
                                const usernameStillInDOM = document.body.contains(usernameInput);
                                const passwordStillInDOM = document.body.contains(passwordInput);
            
                                if (!usernameStillInDOM || !passwordStillInDOM) {
                                    console.log("%c[Alert] Elemen input dihapus dari DOM setelah injeksi!", "color: red; font-weight: bold");
                                }
            
                                const newUsernameElement = document.querySelector('input[name="login"]');
                                if (newUsernameElement && newUsernameElement !== usernameInput) {
                                    console.log("%c[Alert] Elemen username sepertinya dibuat ulang!", "color: red; font-weight: bold");
                                }
            
                                if (timeout === timeouts[timeouts.length - 1]) {
                                    console.log("%c[Final Analysis] Hasil akhir pengujian keamanan:", "color: blue; font-weight: bold");
            
                                    if (usernameSuccess && passwordSuccess && usernameSame && passwordSame) {
                                        console.log("%c[Vulnerable] Website rentan terhadap injeksi nilai input!", "color: red; font-weight: bold");
                                    } else if (!usernameSuccess && !passwordSuccess) {
                                        console.log("%c[Secure] Website menolak injeksi nilai input", "color: green; font-weight: bold");
                                    } else {
                                        console.log("%c[Partial] Website memiliki beberapa perlindungan terhadap injeksi nilai", "color: orange; font-weight: bold");
                                    }
            
                                    console.log("[Security Recommendations]:");
                                    console.log("- Implementasi Content Security Policy (CSP) yang ketat");
                                    console.log("- Gunakan input validation pada client dan server side");
                                    console.log("- Implementasi anti-CSRF token pada form");
                                    console.log("- Pertimbangkan penggunaan honeypot fields");
                                    console.log("- Monitor dan deteksi perilaku manipulasi DOM");
                                }
                            }, timeout);
                        });
            
                        console.log("[Form Analysis] Mencoba menganalisis submit handler...");
                        const parentForm = usernameInput.closest('form');
                        if (parentForm) {
                            try {
                                const originalSubmit = parentForm.onsubmit;
                                let intercepted = false;
            
                                parentForm.onsubmit = function(e) {
                                    console.log("%c[Alert] Form submit terdeteksi!", "color: orange");
                                    e.preventDefault();
                                    intercepted = true;
            
                                    parentForm.onsubmit = originalSubmit;
                                    return false;
                                };
            
                                const submitEvent = new Event('submit', { cancelable: true });
                                const submitPrevented = !parentForm.dispatchEvent(submitEvent);
            
                                if (submitPrevented || intercepted) {
                                    console.log("%c[+] Submit handler terdeteksi dan berhasil dicegat", "color: green");
                                } else {
                                    console.log("%c[!] Submit handler tidak terdeteksi atau gagal dicegat", "color: orange");
                                }
            
                                parentForm.onsubmit = originalSubmit;
                            } catch (submitError) {
                                console.log(`%c[-] Error saat mencoba analisis submit: ${submitError.message}`, "color: red");
                            }
                        } else {
                            console.log("[-] Tidak menemukan parent form untuk analisis submit");
                        }
                    }
                } catch (error) {
                    console.error("%c[Security Error] Skrip di-blokir/dideteksi:", "color: red; font-weight: bold", error);
                    console.log("%c[!] Kemungkinan skrip di-blokir oleh:", "color: red");
                    console.log("- Content Security Policy (CSP)");
                    console.log("  Detail CSP:");
            
                    try {
                        const cspMeta = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
                        if (cspMeta) {
                            console.log(`  Meta CSP: ${cspMeta.content}`);
                        } else {
                            console.log("  Tidak ada meta CSP yang terdeteksi");
                        }
            
                        if (window.getComputedStyle) {
                            console.log("  CSP Headers: Tidak dapat diakses melalui JavaScript client-side");
                        }
                    } catch (cspError) {
                        console.log(`  Error saat mencoba mendapatkan CSP: ${cspError.message}`);
                    }
            
                    console.log("- XSS Filter");
                    console.log("- Web Application Firewall");
                    console.log("- JavaScript runtime protection");
                    console.log("- Browser security features");
                    console.log("- Anti-automation systems");
            
                    console.log("[Error Analysis]:");
                    console.log(`- Error name: ${error.name}`);
                    console.log(`- Error message: ${error.message}`);
                    console.log(`- Error stack: ${error.stack || 'tidak tersedia'}`);
            
                    if (error.message && error.message.includes('Content Security Policy')) {
                        console.log("%c[Detected] Definite CSP Violation!", "color: red; font-weight: bold");
                    } else if (error.message && error.message.includes('Permission')) {
                        console.log("%c[Detected] Permission policy or Sandbox restriction!", "color: red; font-weight: bold");
                    }
                } finally {
                    console.log("%c[Security Test] Tes injeksi selesai", "color: blue; font-weight: bold");
                    console.log("[Timestamp End]:", new Date().toISOString());
                }
            })();
            
            	/*
            	(function() {
            		const usernameInput = document.querySelector('input[name="login"]');
            		const passwordInput = document.querySelector('input[name="password"]');
            		if (usernameInput && passwordInput) {
            			usernameInput.value = 'nusantara1445h';
            			passwordInput.value = '13mar2024';
            			}
            	})();
            	*/
            """;
    public String scriptInjectDataSimple = "(function() {" + "function startObserving() {" + "    const targetNode = document.querySelector('.work-area-wrap');" + "    if (!targetNode) {" + "        AndroidInterface.mutationFailure('work-area-wrap', 'Target node not found');" + "        return;" + "    }" + "    console.log('MutationObserver initialized');" + "    AndroidInterface.elementFound('work-area-wrap');" + "    const config = { " + "        childList: true," + "        subtree: true," + "        attributes: true," + "        attributeFilter: ['style']" + "    };" + "    const callback = function(mutationsList) {" + "        console.log('Mutation detected:', mutationsList.length + ' changes');" + "        try {" + "            checkAndCaptureElements();" + "        } catch (e) {" + "            AndroidInterface.mutationFailure('observer-callback', e.message);" + "        }" + "    };" + "    const observer = new MutationObserver(callback);" + "    observer.observe(targetNode, config);" + "    checkAndCaptureElements();" + "}" + "function checkAndCaptureElements() {" + "    const captcha = document.querySelector('.captcha-image');" + "    const input = document.querySelector('.inp-dft');" + "    if (captcha) {" + "        AndroidInterface.elementFound('captcha-image');" + "        console.log('Captcha element found');" + "        try {" + "            const bgImage = window.getComputedStyle(captcha).backgroundImage;" + "            if (bgImage) {" + "                let fullDataUrl = bgImage;" + "                if (bgImage.includes('url(')) {" + "                    fullDataUrl = bgImage.replace(/^url\\(['\"]?/, '').replace(/['\"]?\\)$/, '');" + "                }" + "                console.log('Captcha data URL captured');" + "                AndroidInterface.onCaptchaCaptured(fullDataUrl);" + "                new MutationObserver(function(mutations) {" + "                    const newBg = window.getComputedStyle(captcha).backgroundImage;" + "                    if (newBg !== bgImage) {" + "                        console.log('Captcha image changed');" + "                        let newUrl = newBg;" + "                        if (newBg.includes('url(')) {" + "                            newUrl = newBg.replace(/^url\\(['\"]?/, '').replace(/['\"]?\\)$/, '');" + "                        }" + "                        AndroidInterface.onCaptchaCaptured(newUrl);" + "                    }" + "                }).observe(captcha, { attributes: true, attributeFilter: ['style'] });" + "            } else {" + "                AndroidInterface.mutationFailure('captcha-image', 'Invalid background format');" + "            }" + "        } catch (e) {" + "            AndroidInterface.mutationFailure('captcha-image', e.message);" + "        }" + "    } else {" + "        AndroidInterface.elementNotFound('captcha-image');" + "    }" + "    if (input) {" + "        AndroidInterface.elementFound('inp-dft');" + "        console.log('Input element found');" + "        try {" + "            inputLabel = input.value.trim(); " + "            if (inputLabel !== '') {" + "            AndroidInterface.onInputCaptured(inputLabel);" + "            input.addEventListener('input', function(e) {" + "              newLabel = e.target.value.trim(); " + "              if () {}" + "            });" + "            }" + "        } catch (e) {" + "            AndroidInterface.mutationFailure('inp-dft', e.message);" + "        }" + "    } else {" + "        AndroidInterface.elementNotFound('inp-dft');" + "    }" + "}" + "if (document.readyState === 'loading') {" + "    document.addEventListener('DOMContentLoaded', function() {" + "        console.log('DOM fully loaded');" + "        startObserving();" + "    });" + "} else {" + "    startObserving();" + "}" + "})();";

    public String elementFound = "";
    public String elementNotFound = "";
    public String ImageCaptcha = "";
    public String ImgBase64 = "";
    public String ImgLabel = "";
    public String mutationFailure = "";

    private boolean imageDataAvailable() {
        return !this.ImgBase64.isEmpty() && !this.ImgLabel.isEmpty();
    }

    private String isLabelExist() {
        StartWorking startWorking = startWorkingWeakReference.get(); // Ambil referensi Activity
        return startWorking.matchCaptchaImage(this.ImgBase64);
    }

    private void saveCaptchaDataset() {
        StartWorking startWorking = startWorkingWeakReference.get(); // Ambil referensi Activity
        startWorking.saveBase64ImageToDb(this.ImgBase64, this.ImgLabel);
    }

    @JavascriptInterface
    public void elementFound(String className) {
        /* String msg = "Element found: " + className;
          Log.i("WebAppInterface", msg);
          elementFound = msg;

         */
    }

    @JavascriptInterface
    public void elementNotFound(String className) {
        /*String msg = "Element not found: " + className;
        Log.w("WebAppInterface", msg);
        elementNotFound = msg;
         */
    }

    @JavascriptInterface
    public void newCaptchaFound(String base64) {
        String logMsg = "New Captcha data received (" + base64.length() + " chars)";
        Log.d("WebAppInterface", logMsg);
        ImageCaptcha = base64;
    }

    @JavascriptInterface
    public void onCaptchaCaptured(String base64) {
        String logMsg = "Captcha data received (" + base64.length() + " chars)";
        Log.d("WebAppInterface", logMsg);
        ImgBase64 = base64;
    }

    @JavascriptInterface
    public void onInputCaptured(String label) {
        String logMsg = "Input received: " + (label.isEmpty() ? "<empty>" : label);
        Log.d("WebAppInterface", logMsg);
        ImgLabel = label;
    }

    @JavascriptInterface
    public void onButtonSubmitted(String submitted) {
        StartWorking startWorking = startWorkingWeakReference.get(); // Ambil referensi Activity
        if (submitted.equals("816aef8d8880e9681c755a8597634c3a4f568af30ce1b71d6216d807c8b88953")) {
            if (startWorking == null) {
                Log.w("WebAppInterface", "Activity StartWorking sudah dihancurkan"); // Gunakan metode dari StartWorking
                return;
            }// Jika Activity sudah dihancurkan, keluar
            //startWorking.updateConsoleLog("Test public method StartWorking");
            if (startWorking.isScrapingEnabled()) {
                new Thread(() -> {
                    try {
                        if (imageDataAvailable()) {
                            if (isLabelExist() != null) {
                                startWorking.updateConsoleLog("Already have this Image: " + startWorking.isLabelExist());
                            } else {
                                saveCaptchaDataset();// Gunakan metode dari StartWorking
                            }
                        } else {
                            startWorking.updateConsoleLog("Gambar tidak ditemukan"); // Gunakan metode dari StartWorking
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (startWorking != null) {
                            startWorking.updateConsoleLog("Exception: " + e.getMessage()); // Gunakan metode dari StartWorking
                        }
                    }
                }).start();
            }

        }
    }

    @JavascriptInterface
    public void mutationFailure(String className, String reason) {
        String msg = "Mutation error in " + className + ": " + reason;
        Log.e("WebAppInterface", msg);
        mutationFailure = msg;
    }
}